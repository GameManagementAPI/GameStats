package de.c4vxl.gamestats.stats

import de.c4vxl.gamemanager.gma.player.GMAPlayer
import de.c4vxl.gamemanager.gma.player.GMAPlayer.Companion.gma
import org.bukkit.entity.Player

/**
 * Data object for player stats
 */
data class GameStats(
    val bukkitPlayer: Player,
    var wins: Int,
    var losses: Int,
    var deaths: Int,
    var kills: Int,
    var eliminations: Int,
    var currentWinStreak: Int,
    var bestWinStreak: Int
) {
    /**
     * Returns the game bukkitPlayer instance
     */
    val gamePlayer: GMAPlayer
        get() = bukkitPlayer.gma

    /**
     * Returns the total amount of games the player has played
     */
    val totalGamesPlayed: Int
        get() = wins + losses

    /**
     * Returns the players win/loose ratio
     */
    val winRate: Double
        get() = if (totalGamesPlayed == 0) 0.0 else wins.toDouble() / totalGamesPlayed

    /**
     * Returns the players kill/death ratio
     */
    val killDeathRatio: Double
        get() = if (deaths == 0) 0.0 else kills.toDouble() / deaths

    /**
     * Returns the players wins - losses
     */
    val netWins: Int
        get() = wins - losses




    enum class ActionType {
        WIN,
        LOSS,
        KILL,
        DEATH,
        ELIMINATION
    }

    /**
     * Record when an action happened and save it to the db
     * @param type The action
     */
    fun recordAction(type: ActionType) {
        when (type) {
            ActionType.WIN -> {
                wins++
                currentWinStreak++
                bestWinStreak = maxOf(bestWinStreak, currentWinStreak)
            }
            ActionType.LOSS -> {
                losses++
                currentWinStreak = 0
            }
            ActionType.KILL -> {
                kills++
            }
            ActionType.DEATH -> {
                deaths++
            }
            ActionType.ELIMINATION -> {
                eliminations++
            }
        }
    }
}