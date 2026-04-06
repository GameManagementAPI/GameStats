package de.c4vxl.gamestats.stats.data

/**
 * Object of cached stats
 * @param data The actual statistics stored
 * @param isDirty True if the cache is not in line with the data saved on disk
 */
data class StatsCache(
    val data: GameStats,
    var isDirty: Boolean = false
) {
    fun makeDirty() {
        this.isDirty = true
    }
}