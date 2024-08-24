package dev.asodesu.teamsilly.game

import cloud.commandframework.kotlin.MutableCommandBuilder
import dev.asodesu.origami.engine.scene.Scenes
import dev.asodesu.teamsilly.build.MapData
import dev.asodesu.teamsilly.build.MapDataHandlers
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object GameCommands {

    fun register(builder: MutableCommandBuilder<CommandSender>) {
        builder.registerCopy("start") {
            senderType(Player::class)
            handler {
                val player = it.sender as Player
                val mapData = MapDataHandlers.getOrLoad("city")
                Scenes.register(SillyGameScene(player.world, mapData, "sillygame/sillyscene"))
            }
        }
    }

}