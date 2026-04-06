package de.c4vxl.gamestats

import de.c4vxl.gamemanager.language.Language
import de.c4vxl.gamemanager.utils.ResourceUtils
import de.c4vxl.gamestats.command.StatsCommand
import de.c4vxl.gamestats.command.StatsHologramCommand
import de.c4vxl.gamestats.handler.GameHandler
import de.c4vxl.gamestats.handler.HologramHandler
import de.c4vxl.gamestats.stats.Stats
import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.CommandAPIPaperConfig
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.util.logging.Logger

class Main : JavaPlugin() {
    companion object {
        lateinit var instance: Main
        lateinit var logger: Logger
        lateinit var config: FileConfiguration
    }

    override fun onLoad() {
        instance = this
        Main.logger = this.logger

        // Load CommandAPI
        CommandAPI.onLoad(
            CommandAPIPaperConfig(this)
                .silentLogs(true)
                .verboseOutput(false)
        )
    }

    override fun onEnable() {
        // Enable CommandAPI
        CommandAPI.onEnable()

        // Load config
        saveResource("config.yml", false)
        reloadConfig()
        Main.config = this.config

        // Register language extensions
        ResourceUtils.readResource("langs", Main::class.java).split("\n")
            .forEach { langName ->
                Language.provideLanguageExtension(
                    "gamestats",
                    langName,
                    ResourceUtils.readResource("lang/$langName.yml", Main::class.java)
                )
            }

        // Register command
        StatsHologramCommand
        StatsCommand

        // Register handlers
        GameHandler()
        HologramHandler()

        logger.info("[+] $name has been enabled!")
    }

    override fun onDisable() {
        // Disable CommandAPI
        CommandAPI.onDisable()

        // Save data cache
        Stats.saveAll()

        logger.info("[+] $name has been disabled!")
    }
}