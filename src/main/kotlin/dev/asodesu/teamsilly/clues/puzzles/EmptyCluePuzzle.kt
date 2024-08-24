package dev.asodesu.teamsilly.clues.puzzles

import dev.asodesu.origami.engine.scene.Scene
import dev.asodesu.origami.utilities.miniMessage
import dev.asodesu.teamsilly.clues.CluePuzzle

class EmptyCluePuzzle : CluePuzzle {
    override val name = miniMessage("Empty")

    override fun setupComponents(scene: Scene) {
    }
}