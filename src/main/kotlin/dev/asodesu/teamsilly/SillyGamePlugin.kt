package dev.asodesu.teamsilly

import cloud.commandframework.kotlin.extension.buildAndRegister
import dev.asodesu.origami.engine.Origami
import dev.asodesu.origami.utilities.commands.commandManager
import dev.asodesu.teamsilly.build.MapDataHandlers
import dev.asodesu.teamsilly.config.Config
import dev.asodesu.teamsilly.config.ConfigCommands
import dev.asodesu.teamsilly.game.GameCommands
import org.bukkit.plugin.java.JavaPlugin

class SillyGamePlugin : JavaPlugin() {

    override fun onEnable() {
        Origami.init(this)
        Config.init()

        commandManager.buildAndRegister("sillygame") {
            permission("sillies.admin")
            registerCopy("config") { ConfigCommands.register(this) }
            registerCopy("game") { GameCommands.register(this) }
            registerCopy("build") { MapDataHandlers.register(this) }
        }
    }

}