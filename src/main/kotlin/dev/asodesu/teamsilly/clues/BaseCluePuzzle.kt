package dev.asodesu.teamsilly.clues

import dev.asodesu.origami.engine.scene.Scene
import org.bukkit.Location
import org.bukkit.entity.Player

interface BaseCluePuzzle {
    val id: String
    val name: String
    val startingLocation: Location
    var completeListener: CompleteListener?

    fun setupComponents(scene: Scene)

    interface CompleteListener {
        fun onComplete(player: Player)
    }
}