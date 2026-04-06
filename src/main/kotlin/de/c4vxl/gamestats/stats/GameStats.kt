package de.c4vxl.gamestats.stats

import de.c4vxl.gamemanager.gma.player.GMAPlayer
import de.c4vxl.gamemanager.gma.player.GMAPlayer.Companion.gma
import org.bukkit.entity.Player

/**
 * Data object for player stats
 *
 * @param bukkitPlayer The player
 * @param wins The amount of total games won
 * @param losses The amount of games lost
 * @param deaths The amount of deaths in all games
 * @param kills The amount of kills in all games
 * @param eliminations The amount of eliminations in all games
 * @param currentWinStreak The current win streak
 * @param bestWinStreak The currently best win streak
 * @param killRecord The most amount of kills in a game
 * @param eliminationRecord The most amount of eliminations in a game
 * @param currentKills The current amount of kills in the current game
 * @param currentEliminations The current amount of eliminations in the current game
 */
data class GameStats(
    val bukkitPlayer: Player,
    var wins: Int,
    var losses: Int,
    var deaths: Int,
    var kills: Int,
    var eliminations: Int,
    var currentWinStreak: Int,
    var bestWinStreak: Int,
    var killRecord: Int,
    var eliminationRecord: Int,
    var currentKills: Int,
    var currentEliminations: Int
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
        get() = if (deaths == 0) kills.toDouble() else kills.toDouble() / deaths

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
        // Update values
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
                currentKills++
                killRecord = maxOf(killRecord, currentKills)
            }
            ActionType.DEATH -> {
                deaths++
            }
            ActionType.ELIMINATION -> {
                eliminations++
                currentEliminations++
                eliminationRecord = maxOf(eliminationRecord, currentEliminations)
            }
        }

        // Save
        StatsConfig.save(this)
    }

    override fun toString(): String {
        return "GameStats(bukkitPlayer=${bukkitPlayer.name}, wins=$wins, losses=$losses, deaths=$deaths, kills=$kills, eliminations=$eliminations, currentWinStreak=$currentWinStreak, bestWinStreak=$bestWinStreak, killRecord=$killRecord, eliminationRecord=$eliminationRecord, currentKills=$currentKills, currentEliminations=$currentEliminations)"
    }
}