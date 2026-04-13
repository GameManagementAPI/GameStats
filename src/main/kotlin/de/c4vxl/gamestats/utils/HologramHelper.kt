package de.c4vxl.gamestats.utils

import de.c4vxl.gamemanager.language.Language.Companion.language
import de.c4vxl.gamestats.Main
import de.c4vxl.gamestats.stats.Leaderboard
import de.c4vxl.gamestats.stats.data.type.Statistic
import io.papermc.paper.adventure.PaperAdventure
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.server.level.ServerEntity
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.decoration.ArmorStand
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType
import java.util.*
import java.util.function.Predicate

/**
 * A helper class for creating holograms
 */
object HologramHelper {
    /**
     * Holds a list of all markers
     */
    var markers: MutableList<UUID>
        get() = Main.config.getStringList("holograms").map { UUID.fromString(it) }.toMutableList()
        set(value) {
            Main.config.set("holograms", value.map { it.toString() })
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
     * Constructs a hologram from its marker and sends it to a player
     * @param player The player to send the marker to
     * @param markerID The uuid of the marker entity
     */
    fun sendFromMarker(player: Player, markerID: UUID) {
        // Get marker entity
        val marker = player.world.getEntity(markerID) ?: return

        // Utility function
        fun <C : Any> get(key: String, type: PersistentDataType<*, C>): C? =
            marker.persistentDataContainer.get(NamespacedKey.minecraft("gamestats_hologram_$key"), type)

        // Get data
        var lines = get("lines", PersistentDataType.LIST.strings()) ?: listOf()

        if (lines.first().startsWith("leaderboard_"))
            lines = buildLeaderboardLines(
                player,
                Statistic.valueOf(lines.first().removePrefix("leaderboard_"))
            )

        // Send hologram
        Bukkit.getScheduler().callSyncMethod(Main.instance) {
            sendHologram(
                player,
                marker.location,
                *lines.toTypedArray()
            )
        }
    }

    private fun buildLeaderboardLines(player: Player, statistic: Statistic): List<String> {
        val leaderboard = Leaderboard.getTop(statistic)
        val language = player.language.child("gamestats")

        val ownPlace = Leaderboard.getPlace(player.uniqueId, statistic)

        return buildList {
            add("_leaderboard")
            add("_sep")

            if (leaderboard.isEmpty())
                addAll(listOf(
                    language.get("leaderboard.empty.l1"),
                    language.get("leaderboard.empty.l2")
                ))

            for (i in 1..10) {
                val entry = leaderboard.getOrNull(i - 1) ?: break
                val pl = Bukkit.getOfflinePlayer(entry.first)

                add(language.get(
                    "leaderboard.${if (i <= 3) "top3" else "bottom"}",
                    "$i",
                    pl.name ?: break,
                    MiniMessage.miniMessage().serialize(StatsHelper.getStatisticLine(pl, statistic.key, language))
                ))
            }

            add("_sep")

            ownPlace?.let {
                add(language.get(
                    "leaderboard.self",
                    it.toString(),
                    MiniMessage.miniMessage().serialize(StatsHelper.getStatisticLine(player, statistic.key, language))
                ))
            }
        }.reversed()
    }

    /**
     * Spawns a marker
     * @param location The location
     * @param lines The lines of the hologram
     */
    fun spawnMarker(location: Location, vararg lines: String) {
        val marker = location.world.spawnEntity(location, org.bukkit.entity.EntityType.ARMOR_STAND, true)
        marker.isInvisible = true
        marker.isInvulnerable = true
        marker.setGravity(false)
        marker.isPersistent = true

        fun <C : Any> set(key: String, type: PersistentDataType<*, C>, value: C) =
            marker.persistentDataContainer.set(NamespacedKey.minecraft("gamestats_hologram_$key"), type, value)

        set("lines", PersistentDataType.LIST.strings(), lines.toList())

        // Register marker
        markers = markers.apply { add(marker.uniqueId) }
    }

    /**
     * Updates the hologram for a player
     * @param player The player
     */
    fun update(player: Player) {
        // Remove all
        holograms.remove(player)?.let {
            it.forEach { id ->
                (player as CraftPlayer).handle.connection
                    .send(ClientboundRemoveEntitiesPacket(id))
            }
        }

        // Send holograms
        markers.forEach { sendFromMarker(player, it) }
    }
}