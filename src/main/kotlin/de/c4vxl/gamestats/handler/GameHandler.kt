package de.c4vxl.gamestats.handler

import de.c4vxl.gamemanager.gma.event.player.*
import de.c4vxl.gamemanager.gma.player.GMAPlayer.Companion.gma
import de.c4vxl.gamestats.Main
import de.c4vxl.gamestats.stats.GameStats
import de.c4vxl.gamestats.stats.Statistic
import de.c4vxl.gamestats.stats.Stats
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
        Stats.modify(event.player) { recordAction(Statistic.WIN) }
    }

    @EventHandler
    fun onLoose(event: GamePlayerLooseEvent) {
        Stats.modify(event.player) { recordAction(Statistic.LOSS) }
    }

    @EventHandler
    fun onKill(event: PlayerDeathEvent) {
        val player = event.player
        val killer = player.killer ?: return

        if (!player.gma.isInGame) return
        if (!killer.gma.isInGame) return

        Stats.modify(killer) { recordAction(Statistic.KILL) }
    }

    @EventHandler
    fun onDeath(event: GamePlayerDeathEvent) {
        Stats.modify(event.player) { recordAction(Statistic.DEATH) }
    }

    @EventHandler
    fun onEliminate(event: GamePlayerEliminateEvent) {
        event.killer?.let {
            Stats.modify(it) { recordAction(Statistic.ELIMINATION) }
        }
    }

    @EventHandler
    fun onGameQuit(event: GamePlayerQuitEvent) {
        Stats.modify(event.player.bukkitPlayer) {
            currentKills = 0
            currentEliminations = 0
            if (event.game.isRunning)
                losses++
        }
    }
}