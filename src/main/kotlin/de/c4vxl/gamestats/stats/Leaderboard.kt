package de.c4vxl.gamestats.stats

import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Handles global leaderboards
 */
object Leaderboard {
    private val positionCache = ConcurrentHashMap<Statistic, Map<UUID, Int>>()

    /**
     * Rebuilds leaderboard caches
     */
    fun rebuildCache() {
        Statistic.entries.forEach { type ->
            // Build position cache
            positionCache[type] = getLeaderboard(type)
                .mapIndexed { index, (uuid, _) -> uuid to index + 1 }
                .toMap()
        }
    }

    /**
     * Returns a leaderboard to a certain statistic
     */
    fun getLeaderboard(type: Statistic): List<Pair<UUID, Double>> =
        Stats.getAll()
            .map { it.uuid to it.getValue(type).toDouble() }
            .sortedByDescending { it.second }

    /**
     * Returns the top n elements in the leaderboard
     * @param limit The amount of entries to fetch
     * @param type The type of statistic to track by
     */
    fun getTop(limit: Int = 10, type: Statistic): List<Pair<UUID, Double>> =
        getLeaderboard(type)
            .take(limit)
            .map { it.first to it.second }

    /**
     * Returns the position of a player in the leaderboard
     * @param uuid The uuid of the player to look up
     * @param type The type of the leaderboard
     */
    fun getPlace(uuid: UUID, type: Statistic): Int? =
        positionCache[type]!![uuid]
}