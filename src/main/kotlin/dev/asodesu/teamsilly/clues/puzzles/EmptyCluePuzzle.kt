package dev.asodesu.teamsilly.clues.puzzles

import dev.asodesu.origami.engine.scene.Scene
import dev.asodesu.origami.utilities.miniMessage
import dev.asodesu.teamsilly.clues.BaseCluePuzzle
import dev.asodesu.teamsilly.clues.CluePuzzle
import org.bukkit.Location
import org.bukkit.util.BoundingBox

class EmptyCluePuzzle : BaseCluePuzzle {
    override val id: String = "empty"
    override val name: String = "Empty"
    override val startingLocation: Location = Location(null, 0.0, 0.0, 0.0)
    override var completeListener: BaseCluePuzzle.CompleteListener? = null

    override fun setupComponents(scene: Scene) {
    }
}