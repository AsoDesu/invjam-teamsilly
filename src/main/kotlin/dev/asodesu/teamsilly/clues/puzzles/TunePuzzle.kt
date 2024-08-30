package dev.asodesu.teamsilly.clues.puzzles

import dev.asodesu.teamsilly.build.MapData
import dev.asodesu.teamsilly.clues.CluePuzzle
import org.bukkit.World

class TunePuzzle(id: String, val world: World, mapData: MapData) : CluePuzzle(id, mapData, world) {
    override val name: String = "Match-the-tune"
    override val kitRequirement: String = "Wind Charges"
}