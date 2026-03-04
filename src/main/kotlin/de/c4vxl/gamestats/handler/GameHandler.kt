package de.c4vxl.gamestats.handler

import de.c4vxl.gamemanager.gma.event.player.*
import de.c4vxl.gamemanager.gma.player.GMAPlayer.Companion.gma
import de.c4vxl.gamestats.Main
import de.c4vxl.gamestats.stats.GameStats
import de.c4vxl.gamestats.stats.StatsConfig
import de.c4vxl.gamestats.stats.StatsConfig.stats
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent

class GameHandler : Listener {
    init {
        Bukkit.getPluginManager().registerEvents(this, Main.instance)
    }

    @EventHandler
    fun onWin(event: GamePlayerWinEvent) {
        event.player.stats.recordAction(GameStats.ActionType.WIN)
    }

    @EventHandler
    fun onLoose(event: GamePlayerLooseEvent) {
        event.player.stats.recordAction(GameStats.ActionType.LOSS)
    }

    @EventHandler
    fun onKill(event: PlayerDeathEvent) {
        val player = event.player
        val killer = player.killer ?: return

        if (!player.gma.isInGame) return
        if (!killer.gma.isInGame) return

        killer.gma.stats.recordAction(GameStats.ActionType.KILL)
    }

    @EventHandler
    fun onDeath(event: GamePlayerDeathEvent) {
        event.player.stats.recordAction(GameStats.ActionType.DEATH)
    }

    @EventHandler
    fun onEliminate(event: GamePlayerEliminateEvent) {
        event.killer?.stats?.recordAction(GameStats.ActionType.ELIMINATION)
    }

    @EventHandler
    fun onGameQuit(event: GamePlayerQuitEvent) {
        event.player.stats.apply {
            currentKills = 0
            currentEliminations = 0
            losses++

            StatsConfig.save(this)
        }
    }
}