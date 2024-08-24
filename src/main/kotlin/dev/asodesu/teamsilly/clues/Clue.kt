package dev.asodesu.teamsilly.clues

import dev.asodesu.teamsilly.config.Locations
import org.bukkit.Location
import org.bukkit.inventory.ItemStack

class Clue(
    val id: String,
    val name: String,
    val item: ItemStack,
    val puzzle: CluePuzzle
) {
    val compassTarget get() = Locations.values["$id.starting"]?.get() as Location
}