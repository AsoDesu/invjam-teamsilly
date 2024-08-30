package dev.asodesu.teamsilly.clues.puzzles

import dev.asodesu.origami.engine.wiring.annotations.Subscribe
import dev.asodesu.teamsilly.build.MapData
import dev.asodesu.teamsilly.build.element.all
import dev.asodesu.teamsilly.clues.CluePuzzle
import org.bukkit.World
import org.bukkit.event.player.PlayerMoveEvent

open class MovementChallenge(
    id: String,
    override val name: String,
    val world: World,
    mapData: MapData,
    override val kitRequirement: String = "Any"
) : CluePuzzle(id, mapData, world) {
    private val finishRegion = mapData.boundingBoxes.all("puzzle_finish").withPuzzleId().single().box

    @Subscribe
    fun move(evt: PlayerMoveEvent) {
        if (this.isCompleted) return
        if (finishRegion.contains(evt.to.toVector())) {
            this.complete(evt.player)
        }
    }
}