package de.c4vxl.gamestats.command

import de.c4vxl.gamemanager.language.Language
import de.c4vxl.gamemanager.language.Language.Companion.language
import de.c4vxl.gamemanager.plugin.enums.Permission
import de.c4vxl.gamestats.utils.HologramHelper
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.kotlindsl.*
import org.bukkit.Bukkit

/**
 * Command for configuring stats hologram
 */
object StatsHologramCommand {
    val command = commandTree("stats-hologram") {
        withFullDescription(Language.default.child("gamestats").get("command.stats-hologram.desc"))
        withUsage("/stats-hologram <create|remove> [options]")
        withPermission("${Permission.COMMAND_PREFIX.string}.stats-hologram")

        literalArgument("remove") {
            anyExecutor { sender, _ ->
                if (HologramHelper.hologramPosition == null) {
                    sender.sendMessage(sender.language.child("gamestats").getCmp("command.stats-hologram.remove.failure.already"))
                    return@anyExecutor
                }

                HologramHelper.hologramPosition = null
                HologramHelper.hologramLines = listOf()

                // Update for every player
                Bukkit.getOnlinePlayers().forEach { HologramHelper.update(it) }

                sender.sendMessage(sender.language.child("gamestats").getCmp("command.stats-hologram.remove.success"))
            }
        }

        literalArgument("create") {
            greedyStringArgument("lines") {
                replaceSuggestions(ArgumentSuggestions.strings { HologramHelper.possibleStatistics })

                playerExecutor { player, args ->
                    // Set lines
                    HologramHelper.hologramLines =
                        args.get("lines").toString()
                            .split(" ")
                            .filter { it in HologramHelper.possibleStatistics }

                    // Set location
                    HologramHelper.hologramPosition = player.location

                    // Update for every player
                    Bukkit.getOnlinePlayers().forEach { HologramHelper.update(it) }

                    player.sendMessage(player.language.child("gamestats").getCmp("command.stats-hologram.create.success"))
                }
            }
        }
    }
}