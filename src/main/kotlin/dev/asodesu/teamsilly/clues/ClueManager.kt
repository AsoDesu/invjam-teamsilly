package dev.asodesu.teamsilly.clues

import dev.asodesu.origami.engine.Behaviour
import dev.asodesu.origami.engine.add
import dev.asodesu.origami.engine.addBy
import dev.asodesu.origami.engine.consumeCooldown
import dev.asodesu.origami.engine.wiring.annotations.PostApply
import dev.asodesu.origami.engine.wiring.annotations.Subscribe
import dev.asodesu.origami.utilities.bukkit.off
import dev.asodesu.origami.utilities.bukkit.play
import dev.asodesu.origami.utilities.error
import dev.asodesu.origami.utilities.play
import dev.asodesu.origami.utilities.sound
import dev.asodesu.teamsilly.build.MapData
import dev.asodesu.teamsilly.clues.puzzles.EmptyCluePuzzle
import dev.asodesu.teamsilly.clues.puzzles.ParkourPuzzle
import dev.asodesu.teamsilly.clues.puzzles.RedstonePuzzle
import dev.asodesu.teamsilly.clues.slots.ClueSlot
import dev.asodesu.teamsilly.game.SillyGameScene
import org.bukkit.Material
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import kotlin.time.Duration.Companion.seconds

class ClueManager(val scene: SillyGameScene, val mapData: MapData) : Behaviour() {
    private var index = 0
    val clues = listOf(
        Clue("testClue1", "Test Clue 1", ItemStack(Material.SEA_PICKLE), ParkourPuzzle("testClue1", scene.world, mapData)),
        Clue("testClue2", "Test Clue 2", ItemStack(Material.RED_CANDLE), RedstonePuzzle("testClue2", scene.world, mapData)),
        Clue("testClue3", "Test Clue 3", ItemStack(Material.DIAMOND_SWORD), EmptyCluePuzzle()),
        Clue("testClue4", "Test Clue 4", ItemStack(Material.REDSTONE), EmptyCluePuzzle()),
        Clue("testClue5", "Test Clue 5", ItemStack(Material.LEATHER_BOOTS), EmptyCluePuzzle()),
    )
    private val clueSlots = Array(4) { i ->
        ClueSlot(i, scene, mapData, this)
    }

    @PostApply
    private fun apply() {
        clues.forEach {
            it.setupComponents(scene)
        }
        clueSlots.forEach { slot ->
            slot.setClue(nextClue())
            scene.add(instance = slot)
        }
    }

    fun nextClue(): Clue? {
        return clues.getOrNull(index++)
    }
}