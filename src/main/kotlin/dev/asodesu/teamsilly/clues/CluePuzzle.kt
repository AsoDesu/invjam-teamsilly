package dev.asodesu.teamsilly.clues

import dev.asodesu.origami.engine.Behaviour
import dev.asodesu.origami.engine.add
import dev.asodesu.origami.engine.scene.Scene
import dev.asodesu.origami.engine.wiring.annotations.Subscribe
import dev.asodesu.origami.utilities.play
import dev.asodesu.origami.utilities.sendTitle
import dev.asodesu.teamsilly.build.MapData
import dev.asodesu.teamsilly.build.element.MapElement
import dev.asodesu.teamsilly.build.element.all
import dev.asodesu.teamsilly.build.element.resolveSingle
import dev.asodesu.teamsilly.build.element.withAttribute
import dev.asodesu.teamsilly.utils.SOUND_PUZZLES_ENTER
import dev.asodesu.teamsilly.utils.SOUND_PUZZLES_FINISH
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.util.BoundingBox

abstract class CluePuzzle(override val id: String, mapData: MapData, world: World) : Behaviour(), BaseCluePuzzle {
    override val startingLocation = mapData.getTarget(world)
    open val regions = mapData.getRegion()

    override fun setupComponents(scene: Scene) {
        scene.add(this)
    }

    var isCompleted: Boolean = false
    override var completeListener: BaseCluePuzzle.CompleteListener? = null
    fun complete(player: Player) {
        isCompleted = true
        player.play(SOUND_PUZZLES_FINISH)
        completeListener?.onPuzzleComplete(player)
        this.destroy()
    }

    @Subscribe
    fun enter(evt: PlayerMoveEvent) {
        var movingFrom = false
        var movingTo = false

        regions.forEach { box ->
            if (box.contains(evt.from.toBlockLocation().toVector())) movingFrom = true
            if (box.contains(evt.to.toBlockLocation().toVector())) movingTo = true
        }

        if (!movingFrom && movingTo) {
            evt.player.sendTitle(subtitle = "<aqua><obf>[]</obf></aqua> $name Based Lock <red><obf>[]</obf>")
            evt.player.play(SOUND_PUZZLES_ENTER)
        }
    }

    fun <T : MapElement<*>> List<T>.withPuzzleId(): List<T> {
        return this.withAttribute("puzzle_id", id)
    }

    private fun MapData.getTarget(world: World): Location {
        return positions.all("puzzle_target").withPuzzleId().resolveSingle(world)
    }

    private fun MapData.getRegion(): List<BoundingBox> {
        return boundingBoxes.all("puzzle_region").withPuzzleId()
            .map { it.box.expand(0.0, 0.0, 0.0, 1.0, 1.0, 1.0) }
    }
}