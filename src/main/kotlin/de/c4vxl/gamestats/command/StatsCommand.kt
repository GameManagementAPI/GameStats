package de.c4vxl.gamestats.command

import de.c4vxl.gamemanager.language.Language
import de.c4vxl.gamemanager.language.Language.Companion.language
import de.c4vxl.gamestats.utils.StatsHelper
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.playerExecutor
import dev.jorel.commandapi.kotlindsl.stringArgument
import net.kyori.adventure.text.minimessage.MiniMessage
import kotlin.jvm.optionals.getOrNull

/**
 * Command for viewing stats
 */
object StatsCommand {
    val command = commandTree("stats") {
        withFullDescription(Language.default.child("gamestats").get("command.stats.desc"))
        withUsage("/stats [statistic]")

        val possible = StatsHelper.possibleStatistics.filterNot { it.startsWith("_") }

        stringArgument("statistic", optional = true) {
            replaceSuggestions(ArgumentSuggestions.strings { possible.toTypedArray() })

            playerExecutor { player, args ->
                val stat = args.getOptional("statistic").getOrNull()?.toString()
                val lang = player.language.child("gamestats")

                if (stat != null) {
                    player.sendMessage(
                        lang
                            .getCmp("command.stat.msg.specific.l1", stat)
                            .appendNewline()
                            .append(
                                lang.getCmp("command.stat.msg.l2",
                                    MiniMessage.miniMessage().serialize(StatsHelper.getStatisticLine(player, stat))
                                )
                            )
                    )
                    return@playerExecutor
                }

                // Build component
                var component = lang.getCmp("command.stat.msg.all.l1")
                possible.forEach {
                    component = component.appendNewline().append(
                        lang.getCmp("command.stat.msg.l2",
                            MiniMessage.miniMessage().serialize(StatsHelper.getStatisticLine(player, it))
                        )
                    )
                }

                player.sendMessage(component)
            }
        }
    }
}