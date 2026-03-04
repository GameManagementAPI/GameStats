package de.c4vxl.gamestats.handler

import de.c4vxl.gamemanager.gma.event.player.GamePlayerJoinedEvent
import de.c4vxl.gamemanager.gma.event.player.GamePlayerQuitEvent
import de.c4vxl.gamestats.Main
import de.c4vxl.gamestats.utils.HologramHelper
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerJoinEvent

class HologramHandler : Listener {
    init {
        Bukkit.getPluginManager().registerEvents(this, Main.instance)
    }

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        HologramHelper.update(event.player)
    }

    @EventHandler
    fun onWorldChange(event: PlayerChangedWorldEvent) {
        HologramHelper.update(event.player)
    }

    @EventHandler
    fun onGameJoin(event: GamePlayerJoinedEvent) {
        HologramHelper.update(event.player.bukkitPlayer)
    }

    @EventHandler
    fun onGameQuit(event: GamePlayerQuitEvent) {
        HologramHelper.update(event.player.bukkitPlayer)
    }
}