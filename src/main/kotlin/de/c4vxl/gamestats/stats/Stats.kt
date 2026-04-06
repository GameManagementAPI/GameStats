package de.c4vxl.gamestats.stats

import de.c4vxl.gamemanager.gma.player.GMAPlayer
import de.c4vxl.gamestats.Main
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Central access point for statistics data
 */
object Stats {
    /**
     * Holds a cache of the data in the db
     */
    private val cache = ConcurrentHashMap<UUID, StatsCache>()

    /**
     * Holds a cache of all db keys
     */
    private val allUUIDs: List<UUID> by lazy {
        StatsConfig.dbDir.listFiles { file, _ -> file.isFile }
            ?.map { UUID.fromString(it.nameWithoutExtension) }
            ?: emptyList()
    }

    init {
        // Register automatic saving of data cache
        Bukkit.getScheduler().runTaskTimerAsynchronously(Main.instance, Runnable {
            saveAll()
            Leaderboard.rebuildCache()
        }, 0, 20 * 60 * 10)
    }

    /**
     * Loads all existing statistics into cache
     */
    fun getAll(): List<GameStats> =
        allUUIDs.map { get(it) }

    /**
     * Gets the data of a specific player
     * @param uuid The uuid of the player
     */
    fun get(uuid: UUID): GameStats =
        cache.computeIfAbsent(uuid) {
            StatsCache(StatsConfig.get(uuid))
        }.data

    /**
     * Reloads the statistics in cache of a specific user
     * @param uuid The uuid of the player
     */
    fun reload(uuid: UUID) {
        cache[uuid] = StatsCache(StatsConfig.get(uuid))
    }

    /**
     * Saves a specific cache to disk
     * @param uuid The players uuid the cache belongs to
     */
    fun save(uuid: UUID) {
        val cached = cache[uuid] ?: return

        // Cache hasn't changed
        if (!cached.isDirty) return

        // Save
        StatsConfig.save(cached.data)

        // Set cache to clean
        cached.isDirty = false
    }

    /**
     * Saves all caches to disk
     */
    fun saveAll() {
        Main.logger.info("Saving statistics cache to db...")
        cache.forEach { (id, _) -> save(id) }
    }

    /**
     * Makes a cache of a specific entry dirty
     */
    fun makeDirty(uuid: UUID) =
        cache[uuid]?.makeDirty()

    /**
     * Allows for modifying of a statistic object and automatically marks it as dirty after for it to be saved to disk
     * @param uuid The uuid of the statistics holder
     * @param block The actions to perform on the statistic
     */
    inline fun modify(uuid: UUID, block: GameStats.() -> Unit) {
        val data = get(uuid)
        data.apply(block)
        makeDirty(uuid)
    }

    /**
     * Allows for modifying of a statistic object and automatically marks it as dirty after for it to be saved to disk
     * @param player The player holding the stats
     * @param block The actions to perform on the statistic
     */
    inline fun modify(player: OfflinePlayer, block: GameStats.() -> Unit) =
        modify(player.uniqueId, block)

    /**
     * Allows for modifying of a statistic object and automatically marks it as dirty after for it to be saved to disk
     * @param player The player holding the stats
     * @param block The actions to perform on the statistic
     */
    inline fun modify(player: GMAPlayer, block: GameStats.() -> Unit) =
        modify(player.bukkitPlayer, block)

    /**
     * Returns the statistics of the player
     */
    val OfflinePlayer.stats
        get() = get(this.uniqueId)

    /**
     * Returns the statistics of the player
     */
    val GMAPlayer.stats
        get() = this.bukkitPlayer.stats
}