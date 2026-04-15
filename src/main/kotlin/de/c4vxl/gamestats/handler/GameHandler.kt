package de.c4vxl.gamestats.handler

import de.c4vxl.gamemanager.gma.event.player.*
import de.c4vxl.gamemanager.gma.player.GMAPlayer.Companion.gma
import de.c4vxl.gamestats.Main
import de.c4vxl.gamestats.stats.Stats
import de.c4vxl.gamestats.stats.data.type.Statistic
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
        // Don't count private games
        if (event.game.isPrivate)
            return

        Stats.modify(event.player) { recordAction(Statistic.WIN) }
    }

    @EventHandler
    fun onLoose(event: GamePlayerLoseEvent) {
        // Don't count private games
        if (event.game.isPrivate)
            return

        Stats.modify(event.player) { recordAction(Statistic.LOSS) }
    }

    @EventHandler
    fun onKill(event: PlayerDeathEvent) {
        val player = event.player
        val killer = player.killer ?: return

        if (!player.gma.isInGame) return
        if (!killer.gma.isInGame) return

        // Don't count private games
        if (event.player.gma.game?.isPrivate ?: return)
            return

        Stats.modify(killer) { recordAction(Statistic.KILL) }
    }

    @EventHandler
    fun onDeath(event: GamePlayerDeathEvent) {
        // Don't count private games
        if (event.game.isPrivate)
            return

        Stats.modify(event.player) { recordAction(Statistic.DEATH) }
    }

    @EventHandler
    fun onEliminate(event: GamePlayerEliminateEvent) {
        // Don't count private games
        if (event.game.isPrivate)
            return

        event.killer?.let {
            Stats.modify(it) { recordAction(Statistic.ELIMINATION) }
        }
    }

    @EventHandler
    fun onGameQuit(event: GamePlayerQuitEvent) {
        // Don't count private games
        if (event.game.isPrivate)
            return

        Stats.modify(event.player.bukkitPlayer) {
            currentKills = 0
            currentEliminations = 0
            if (event.game.isRunning)
                losses++
        }
    }
}