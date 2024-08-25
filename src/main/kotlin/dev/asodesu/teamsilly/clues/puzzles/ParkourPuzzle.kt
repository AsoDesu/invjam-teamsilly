package dev.asodesu.teamsilly.clues.puzzles

import dev.asodesu.origami.engine.wiring.annotations.Subscribe
import dev.asodesu.origami.utilities.miniMessage
import dev.asodesu.teamsilly.build.MapData
import dev.asodesu.teamsilly.build.element.all
import dev.asodesu.teamsilly.clues.CluePuzzle
import org.bukkit.World
import org.bukkit.event.player.PlayerMoveEvent

class ParkourPuzzle(id: String, val world: World, mapData: MapData) : CluePuzzle(id, mapData, world) {
    override val name = "Parkour"
    private val finishRegion = mapData.boundingBoxes.all("parkour_finish").withPuzzleId().single().box

    @Subscribe
    fun move(evt: PlayerMoveEvent) {
        if (this.isCompleted) return

        if (finishRegion.contains(evt.to.toVector())) {
            this.complete(evt.player)
        }
    }
}