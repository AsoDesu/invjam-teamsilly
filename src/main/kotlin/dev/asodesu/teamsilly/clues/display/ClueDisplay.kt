package dev.asodesu.teamsilly.clues.display

import dev.asodesu.origami.engine.Destroyable

interface ClueDisplay : Destroyable {
    fun update()
}