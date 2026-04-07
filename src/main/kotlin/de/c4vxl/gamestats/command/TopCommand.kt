package de.c4vxl.gamestats.command

import de.c4vxl.gamemanager.language.Language
import de.c4vxl.gamemanager.language.Language.Companion.language
import de.c4vxl.gamestats.Main
import de.c4vxl.gamestats.stats.Leaderboard
import de.c4vxl.gamestats.stats.data.type.Statistic
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.playerExecutor
import dev.jorel.commandapi.kotlindsl.stringArgument
import org.bukkit.Bukkit
import kotlin.jvm.optionals.getOrNull

/**
 * Command for viewing stats
 */
object TopCommand {
    val command = commandTree("top") {
        withFullDescription(Language.default.child("gamestats").get("command.top.desc"))
        withUsage("/top [statistic]")
        withAliases("leaderboard")


        stringArgument("statistic", optional = true) {
            replaceSuggestions(ArgumentSuggestions.strings { Statistic.entries.map { it.name.lowercase() }.toTypedArray() })

            playerExecutor { player, args ->
                val statistic = args.getOptional("statistic").getOrNull()?.toString()
                    ?: Main.config.getString("config.default-leaderboard-statistic")
                    ?: "WIN"

                val leaderboard = Leaderboard.getTop(Statistic.valueOf(statistic.uppercase()))
                val lang = player.language.child("gamestats")

                // Build component
                var component = lang.getCmp("command.top.msg.all.l1")

                for (i in 1..10) {
                    val entry = leaderboard.getOrNull(i - 1)
                    val uuid = entry?.first ?: break
                    val name = Bukkit.getOfflinePlayer(uuid).name ?: break

                    component = component
                        .appendNewline()
                        .append(lang.getCmp("command.top.msg.all.l2", i.toString(), name, entry.second.toString()))
                }

                player.sendMessage(component)
            }
        }
    }
}