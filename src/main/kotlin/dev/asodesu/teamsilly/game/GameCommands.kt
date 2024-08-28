package dev.asodesu.teamsilly.game

import cloud.commandframework.arguments.standard.IntegerArgument
import cloud.commandframework.kotlin.MutableCommandBuilder
import dev.asodesu.origami.engine.get
import dev.asodesu.origami.engine.scene.Scenes
import dev.asodesu.origami.utilities.error
import dev.asodesu.teamsilly.build.MapDataHandlers
import dev.asodesu.teamsilly.clues.ClueManager
import dev.asodesu.teamsilly.clues.display.ActiveClueDisplay
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

        builder.registerCopy("debug") {
            registerCopy("clue_slot") {
                argument(IntegerArgument.of("slot"))
                registerCopy("recreate") {
                    handler {
                        val slotIndex = it.get<Int>("slot")
                        val scene = Scenes.map["sillygame/sillyscene"] as SillyGameScene
                        val slot = scene.get<ClueManager>().clueSlots.getOrNull(slotIndex)
                            ?: return@handler it.sender.error("Invalid slot.")

                        slot.display?.destroy()
                        slot.display = null

                        if (slot.clue != null) slot.display = ActiveClueDisplay(slot.displayOrigin, slot.clue!!)
                    }
                }
            }
        }
    }

}