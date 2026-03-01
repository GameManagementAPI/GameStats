package de.c4vxl.gamestats.stats

import de.c4vxl.gamemanager.gma.player.GMAPlayer
import de.c4vxl.gamestats.Main
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import java.io.File

/**
 * Access point to the internal database
 */
object StatsConfig {
    /**
     * Returns the database path
     */
    private val dbDir: File get() =
        File(Main.config.getString("config.db-path") ?: "./stats/")
            .also { it.mkdirs() }

    /**
     * Returns the config of a bukkitPlayer
     */
    private fun getConfig(player: Player): YamlConfiguration =
        YamlConfiguration.loadConfiguration(
            dbDir.resolve(player.uniqueId.toString())
                .also { it.createNewFile() }
        )

    /**
     * Returns the players game statistics
     */
    val GMAPlayer.stats: GameStats get() =
        get(this.bukkitPlayer)

    /**
     * Retrieves the game stats of a player
     * @param player The player to retrieve the stats of
     */
    fun get(player: Player): GameStats {
        // Load stats
        val config = getConfig(player)
        return GameStats(
            player,
            config.getInt("wins", 0),
            config.getInt("losses", 0),
            config.getInt("deaths", 0),
            config.getInt("kills", 0),
            config.getInt("eliminations", 0),
            config.getInt("currentWinStreak", 0),
            config.getInt("bestWinStreak", 0),
            config.getInt("currentKills", 0),
            config.getInt("currentEliminations", 0)
        )
    }

    /**
     * Saves a statistic of a player
     * @param stats The statistic
     */
    fun save(stats: GameStats) {
        val config = getConfig(stats.bukkitPlayer)

        // Set
        config.set("wins", stats.wins)
        config.set("losses", stats.losses)
        config.set("deaths", stats.deaths)
        config.set("kills", stats.kills)
        config.set("eliminations", stats.eliminations)
        config.set("currentWinStreak", stats.currentWinStreak)
        config.set("bestWinStreak", stats.bestWinStreak)
        config.set("killRecord", stats.killRecord)
        config.set("eliminationRecord", stats.eliminationRecord)
        config.set("currentKills", stats.currentKills)
        config.set("currentEliminations", stats.currentEliminations)

        // Save
        config.save(
            dbDir.resolve(stats.bukkitPlayer.uniqueId.toString())
                .also { it.createNewFile() }
        )
    }
}