package dev.asodesu.teamsilly.clues

import dev.asodesu.origami.engine.Behaviour
import dev.asodesu.origami.engine.consumeCooldown
import dev.asodesu.origami.engine.wiring.annotations.PostApply
import dev.asodesu.origami.engine.wiring.annotations.Subscribe
import dev.asodesu.origami.utilities.bukkit.off
import dev.asodesu.origami.utilities.bukkit.play
import dev.asodesu.origami.utilities.error
import dev.asodesu.origami.utilities.play
import dev.asodesu.origami.utilities.sound
import dev.asodesu.teamsilly.clues.puzzles.EmptyCluePuzzle
import dev.asodesu.teamsilly.clues.slots.ClueSlot
import org.bukkit.Material
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import kotlin.time.Duration.Companion.seconds

class ClueManager : Behaviour() {
    private var index = 0
    private val clues = listOf(
        Clue("testClue1", "Test Clue 1", ItemStack(Material.SEA_PICKLE), EmptyCluePuzzle()),
        Clue("testClue2", "Test Clue 2", ItemStack(Material.RED_CANDLE), EmptyCluePuzzle()),
        Clue("testClue3", "Test Clue 3", ItemStack(Material.DIAMOND_SWORD), EmptyCluePuzzle()),
        Clue("testClue4", "Test Clue 4", ItemStack(Material.REDSTONE), EmptyCluePuzzle()),
    )
    private val clueSlots = Array(4) { ClueSlot(it) }

    @PostApply
    private fun apply() {
        clueSlots.forEach { it.setClue(nextClue()) }
    }

    @Subscribe
    fun interact(evt: PlayerInteractEvent) {
        val block = evt.clickedBlock ?: return
        if (!block.type.name.endsWith("_BUTTON")) return
        evt.isCancelled = true

        if (evt.player.consumeCooldown("sillygame/compass", 5.seconds)) {
            if (evt.player.consumeCooldown("sillygame/compass_warning", 0.5.seconds)) {
                evt.player.error("You're collecting compasses too fast.")
            }
            return
        }

        val blockLocation = block.location.toBlockLocation()
        val slotByButton = clueSlots.find { it.button.location.toBlockLocation() == blockLocation }
        val compass = slotByButton?.getCompass() ?: return

        val direction = Vector(1.0, 0.0, 0.0)

        val dispenserLocation = block.location.toCenterLocation()
        dispenserLocation.world.dropItem(dispenserLocation, compass) {
            it.velocity = direction.multiply(0.3)

            val dispenser = sound("block.dispenser.dispense")
            it.world.play(dispenser, it.location)
        }
    }

    fun nextClue(): Clue? {
        return clues.getOrNull(index++)
    }
}