package de.c4vxl.gamestats.stats.data.type

enum class Statistic(val key: String) {
    WIN("wins"),
    LOSS("losses"),
    KILL("kills"),
    DEATH("deaths"),
    ELIMINATION("eliminations"),
    NET_WINS("netwins"),
    WIN_RATE("winrate")
}