package de.c4vxl.gamestats.command

import de.c4vxl.gamemanager.language.Language
import de.c4vxl.gamemanager.language.Language.Companion.language
import de.c4vxl.gamemanager.plugin.enums.Permission
import de.c4vxl.gamestats.utils.HologramHelper
import de.c4vxl.gamestats.utils.StatsHelper
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.kotlindsl.*
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.entity.EntityType

/**
 * Command for configuring stats hologram
 */
object StatsHologramCommand {
    val command = commandTree("stats-hologram") {
        withFullDescription(Language.default.child("gamestats").get("command.stats-hologram.desc"))
        withUsage("/stats-hologram <create|remove> [options]")
        withPermission("${Permission.COMMAND_PREFIX.string}.stats-hologram")

        literalArgument("remove") {
            playerExecutor { player, _ ->
                // Find nearest marker
                val marker = player.getNearbyEntities(5.0, 5.0, 5.0)
                    .find {
                        it.persistentDataContainer.keys.contains(NamespacedKey.minecraft("gamestats_hologram_lines"))
                    }

                if (marker == null) {
                    player.sendMessage(player.language.child("gamestats").getCmp("command.stats-hologram.remove.failure.already"))
                    return@playerExecutor
                }

                // Remove marker
                marker.remove()

                // Update for every player
                Bukkit.getOnlinePlayers().forEach { HologramHelper.update(it) }

                player.sendMessage(player.language.child("gamestats").getCmp("command.stats-hologram.remove.success"))
            }
        }

        literalArgument("create") {
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

                    player.sendMessage(player.language.child("gamestats").getCmp("command.stats-hologram.create.success"))
                }
            }
        }
    }
}