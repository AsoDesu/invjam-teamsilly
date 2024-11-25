package dev.asodesu.teamsilly.clues.puzzles

import dev.asodesu.origami.engine.wiring.annotations.Subscribe
import dev.asodesu.origami.engine.wiring.annotations.Tick
import dev.asodesu.teamsilly.build.MapData
import dev.asodesu.teamsilly.build.element.all
import dev.asodesu.teamsilly.build.element.resolveSingle
import dev.asodesu.teamsilly.clues.CluePuzzle
import org.bukkit.Vibration
import org.bukkit.World
import org.bukkit.block.data.AnaloguePowerable
import org.bukkit.block.data.Lightable
import org.bukkit.block.data.Powerable
import org.bukkit.event.block.SculkBloomEvent

class RedstonePuzzle(id: String, val world: World, mapData: MapData) : CluePuzzle(id, mapData, world) {
    override val name = "Redstone"
    private val trigger = mapData.positions.all("redstone_trigger").withPuzzleId().resolveSingle(world)

    @Tick
    fun tick() {
        val isTriggered = when(val data = trigger.block.blockData) {
            is Lightable -> data.isLit
            is Powerable -> data.isPowered
            is AnaloguePowerable -> data.power > 0
            else -> false
        }
        if (isTriggered) {
            val player = world.players.minBy { it.location.distance(trigger) }
            complete(player)
        }
    }
}