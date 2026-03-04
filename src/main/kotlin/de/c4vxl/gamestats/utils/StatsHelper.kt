package de.c4vxl.gamestats.utils

import de.c4vxl.gamemanager.gma.player.GMAPlayer.Companion.gma
import de.c4vxl.gamemanager.language.Language
import de.c4vxl.gamemanager.language.Language.Companion.language
import de.c4vxl.gamestats.stats.StatsConfig.stats
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import kotlin.math.round

object StatsHelper {
    /**
     * Returns a list of all possible statistics that can be displayed
     */
    val possibleStatistics =
        Language.default.child("gamestats").translations.keys
            .filter { it.startsWith("stat.text.") }
            .map { it.removePrefix("stat.text.") }
            .toTypedArray()

    /**
     * Returns a line of a specific statistic
     * @param player The player the message should be sent to
     * @param statistic The statistic
     */
    fun getStatisticLine(player: Player, statistic: String): Component {
        // Get statistic
        val stats = player.gma.stats
        val statisticString = when (statistic) {
            "wins" -> stats.wins
            "losses" -> stats.losses
            "deaths" -> stats.deaths
            "kills" -> stats.kills
            "eliminations" -> stats.eliminations
            "currentWinStreak" -> stats.currentWinStreak
            "bestWinStreak" -> stats.bestWinStreak
            "killRecord" -> stats.killRecord
            "eliminationRecord" -> stats.eliminationRecord
            "kd" -> stats.killDeathRatio
            "netwins" -> stats.netWins
            "winrate" -> round(stats.winRate * 100)
            "totalgames" -> stats.totalGamesPlayed
            else -> -1
        }.toString()

        // Get translation
        return player.language.child("gamestats")
            .getCmp(
                "stat.text.$statistic",
                player.name,
                statisticString
            )
    }
}