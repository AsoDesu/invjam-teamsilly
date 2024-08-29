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
import dev.asodesu.teamsilly.clues.puzzles.BuildPuzzle
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
        Clue("sugarClue", "<i>\"sugar\"", "A small sample of <i>\"sugar\"</i> found around the crime scene", ItemStack(Material.SUGAR), ParkourPuzzle("sugarClue", scene.world, mapData)),
        Clue("vialClue", "Blood Vial", "A bright-red vial of blood collected from the victim", ItemStack(Material.RED_CANDLE), RedstonePuzzle("vileClue", scene.world, mapData)),
        Clue("mapClue", "Location Data", "A map with various locations marked, including the murder location", ItemStack(Material.FILLED_MAP), BuildPuzzle("mapClue", scene.world, mapData)), // BuildPuzzle
        Clue("witnessClue", "Witness Statement", "A statement from a witness around the area.", ItemStack(Material.WRITTEN_BOOK), EmptyCluePuzzle()), // Whack a mole
        Clue("shoesClue", "Discarded Shoes", "Light-brown shoes, found around the scene and potentially discarded by the suspect", ItemStack(Material.LEATHER_BOOTS), EmptyCluePuzzle()), // TRIVIA
        Clue("cameraClue", "Camera Log", "A 30-minute long tape from a nearby camera, showing the suspect around the crime scene", ItemStack(Material.NETHERITE_INGOT), EmptyCluePuzzle()), // Trident Challenge
        Clue("weaponClue", "Blood-Stained Axe", "A shining, bright-blue axe, forged from diamonds, fresh from the depths", ItemStack(Material.DIAMOND_AXE), EmptyCluePuzzle()), // Elytra Challenge
        Clue("fibreClue", "Clothes Fibers", "Fibers torn from the suspects clothes and left at the crime scene", ItemStack(Material.NETHERITE_SCRAP), EmptyCluePuzzle()), // WirePuzzle
        Clue("wordleClue", "Motive", "A piece of paper left at the scene, seems to indicate some sort of motive...", ItemStack(Material.PAPER), EmptyCluePuzzle()), // Wordle
        Clue("corpseClue", "The Corpse.", "The corpse of the victim, preserved temporarily", ItemStack(Material.PLAYER_HEAD), EmptyCluePuzzle()), // Maths
        Clue("fingerprintsClue", "Fingerprints", "Fingerprints left on various surfaces in the surrounding area.", ItemStack(Material.GUNPOWDER), EmptyCluePuzzle()), // Search
    )
    val clueSlots = Array(4) { i ->
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

    override fun destroy() {
        clueSlots.forEach { it.destroy() }
    }
}