package de.c4vxl.gamestats.utils

import de.c4vxl.gamemanager.language.Language
import de.c4vxl.gamemanager.language.Language.Companion.language
import de.c4vxl.gamestats.stats.Stats.stats
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.OfflinePlayer
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
     * @param language The target language
     */
    fun getStatisticLine(player: OfflinePlayer, statistic: String, language: Language? = null): Component {
        // Get statistic
        val stats = player.stats
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
            "_sep" -> -1
            "_your_stats" -> -1
            "_leaderboard" -> -1
            "_player_stats" -> -1
            else -> return MiniMessage.miniMessage().deserialize(statistic)
        }.toString()

        // Get translation
        return (language ?: player.player?.language ?: Language.default).child("gamestats")
            .getCmp(
                "stat.text.$statistic",
                player.name ?: "",
                statisticString
            )
    }
}