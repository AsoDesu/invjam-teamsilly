package dev.asodesu.teamsilly.clues

import dev.asodesu.origami.engine.scene.Scene
import net.kyori.adventure.text.Component

interface CluePuzzle {
    val name: Component
    fun setupComponents(scene: Scene)
}