package de.c4vxl.gamestats.utils

import de.c4vxl.gamemanager.language.Language
import de.c4vxl.gamestats.Main
import io.papermc.paper.adventure.PaperAdventure
import net.kyori.adventure.text.Component
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.server.level.ServerEntity
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.decoration.ArmorStand
import org.bukkit.Location
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.entity.Player
import java.util.function.Predicate

/**
 * A helper class for creating holograms
 */
object HologramHelper {
    /**
     * Holds the location where holograms are spawned
     */
    var hologramPosition: Location?
        get() = Main.config.getLocation("hologram.loc")
        set(value) {
            Main.config.set("hologram.loc", value)
            Main.config.save(Main.instance.dataFolder.resolve("config.yml"))
        }

    /**
     * Holds the different lines of the hologram
     */
    var hologramLines: List<String>
        get() = Main.config.getStringList("hologram.lines")
        set(value) {
            Main.config.set("hologram.lines", value)
            Main.config.save(Main.instance.dataFolder.resolve("config.yml"))
        }

    /**
     * Holds all hologram entities for players
     */
    private val holograms = mutableMapOf<Player, MutableList<Int>>()

    /**
     * Sends a hologram of a text to a specific player
     * @param bukkitPlayer The player to send the hologram to
     * @param location The location of the hologram
     * @param text The text to display
     */
    private fun sendHologram(bukkitPlayer: Player, location: Location, text: Component) {
        // Return if wrong world
        if (bukkitPlayer.world != location.world)
            return

        // Get player info
        val player = (bukkitPlayer as CraftPlayer).handle
        val level = player.level()
        val connection = player.connection

        // Create entity
        val entity = ArmorStand(EntityType.ARMOR_STAND, level)
        entity.isMarker = true
        entity.isNoGravity = true
        entity.isInvisible = true
        entity.isCustomNameVisible = true
        entity.customName = PaperAdventure.asVanilla(text)
        entity.setPos(location.x, location.y, location.z)

        // Send entity
        val synchronizer = object : ServerEntity.Synchronizer {
            override fun sendToTrackingPlayers(p0: Packet<in ClientGamePacketListener>) {}
            override fun sendToTrackingPlayersAndSelf(p0: Packet<in ClientGamePacketListener>) {}
            override fun sendToTrackingPlayersFiltered(p0: Packet<in ClientGamePacketListener>, p1: Predicate<ServerPlayer>) {}
        }
        connection.send(
            ClientboundAddEntityPacket(
                entity,
                ServerEntity(level, entity, 0, false, synchronizer, setOf())
            )
        )

        // Update entity data
        connection.send(ClientboundSetEntityDataPacket(entity.id, entity.entityData.packAll()))

        holograms.getOrPut(bukkitPlayer) { mutableListOf() }
            .add(entity.id)
    }

    /**
     * Sends a statistic-hologram to a player
     * @param player The player to send the statistic to
     * @param location The bottom location of the hologram
     * @param lines The lines to add to the hologram
     */
    private fun sendHologram(player: Player, location: Location, vararg lines: String) {
        var y = 0.0
        lines.forEach {
            y += if (it == "sep") 0.5
                 else 0.3

            sendHologram(
                player,
                location.clone().add(0.0, y, 0.0),
                StatsHelper.getStatisticLine(player, it)
            )
        }
    }

    /**
     * Updates the hologram for a player
     * @param player The player
     */
    fun update(player: Player) {
        val pos = hologramPosition ?: return
        val lines = hologramLines.takeIf { it.isNotEmpty() } ?: return

        // Remove all
        holograms.remove(player)?.let {
            it.forEach { id ->
                (player as CraftPlayer).handle.connection
                    .send(ClientboundRemoveEntitiesPacket(id))
            }
        }

        sendHologram(player, pos, *lines.toTypedArray())
    }
}