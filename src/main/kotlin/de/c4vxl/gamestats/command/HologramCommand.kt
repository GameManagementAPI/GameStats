package de.c4vxl.gamestats.command

import de.c4vxl.gamemanager.language.Language
import de.c4vxl.gamemanager.language.Language.Companion.language
import de.c4vxl.gamemanager.plugin.enums.Permission
import de.c4vxl.gamestats.Main
import de.c4vxl.gamestats.stats.data.type.Statistic
import de.c4vxl.gamestats.utils.HologramHelper
import de.c4vxl.gamestats.utils.StatsHelper
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.TextArgument
import dev.jorel.commandapi.kotlindsl.*
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import kotlin.jvm.optionals.getOrNull

/**
 * Command for configuring stats hologram
 */
object HologramCommand {
    val command = commandTree("hologram") {
        withFullDescription(Language.default.child("gamestats").get("command.hologram.desc"))
        withUsage("/hologram <create|remove> [options]")
        withPermission("${Permission.COMMAND_PREFIX.string}.hologram")

        literalArgument("remove") {
            playerExecutor { player, _ ->
                // Find nearest marker
                val marker = player.getNearbyEntities(5.0, 5.0, 5.0)
                    .find {
                        it.persistentDataContainer.keys.contains(NamespacedKey.minecraft("gamestats_hologram_lines"))
                    }

                if (marker == null) {
                    player.sendMessage(player.language.child("gamestats").getCmp("command.hologram.remove.failure.already"))
                    return@playerExecutor
                }

                // Remove marker
                HologramHelper.markers = HologramHelper.markers.apply { remove(marker.uniqueId) }
                marker.remove()

                // Update for every player
                Bukkit.getOnlinePlayers().forEach { HologramHelper.update(it) }

                player.sendMessage(player.language.child("gamestats").getCmp("command.hologram.remove.success"))
            }
        }

        literalArgument("create") {
            literalArgument("leaderboard") {
                argument(TextArgument("criteria").replaceSuggestions(ArgumentSuggestions.strings {
                    Statistic.entries.map { it.name.lowercase() }.toTypedArray()
                }), optional = true) {
                    playerExecutor { player, args ->
                        val criteria = Statistic.valueOf(args.getOptional("criteria").getOrNull()?.toString()
                            ?: Main.config.getString("config.default-leaderboard-statistic")
                            ?: "WIN")

                        // Spawn marker
                        HologramHelper.spawnMarker(
                            player.location,
                            "leaderboard_${criteria.name}"
                        )

                        // Update for every player
                        Bukkit.getOnlinePlayers().forEach { HologramHelper.update(it) }

                        player.sendMessage(player.language.child("gamestats").getCmp("command.hologram.create.success"))
                    }
                }
            }

            literalArgument("custom") {
                greedyStringArgument("lines") {
                    replaceSuggestions(ArgumentSuggestions.strings { StatsHelper.possibleStatistics })

                    playerExecutor { player, args ->
                        // Spawn marker
                        HologramHelper.spawnMarker(
                            player.location,
                            *args.get("lines").toString()
                                .split(" ")
                                .filter { it in StatsHelper.possibleStatistics }
                                .toTypedArray()
                        )

                        // Update for every player
                        Bukkit.getOnlinePlayers().forEach { HologramHelper.update(it) }

                        player.sendMessage(player.language.child("gamestats").getCmp("command.hologram.create.success"))
                    }
                }
            }
        }
    }
}