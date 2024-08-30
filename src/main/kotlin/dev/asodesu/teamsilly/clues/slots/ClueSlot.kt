package dev.asodesu.teamsilly.clues.slots

import dev.asodesu.origami.engine.Behaviour
import dev.asodesu.origami.engine.consumeCooldown
import dev.asodesu.origami.engine.wiring.annotations.Subscribe
import dev.asodesu.origami.utilities.bukkit.debug
import dev.asodesu.origami.utilities.bukkit.play
import dev.asodesu.origami.utilities.error
import dev.asodesu.origami.utilities.miniMessage
import dev.asodesu.origami.utilities.play
import dev.asodesu.origami.utilities.sendTitle
import dev.asodesu.origami.utilities.sound
import dev.asodesu.origami.utilities.success
import dev.asodesu.teamsilly.build.MapData
import dev.asodesu.teamsilly.build.element.all
import dev.asodesu.teamsilly.build.element.noRotation
import dev.asodesu.teamsilly.build.element.withAttribute
import dev.asodesu.teamsilly.clues.Clue
import dev.asodesu.teamsilly.clues.ClueManager
import dev.asodesu.teamsilly.clues.display.ActiveClueDisplay
import dev.asodesu.teamsilly.clues.display.ClueDisplay
import dev.asodesu.teamsilly.config.Locations
import dev.asodesu.teamsilly.game.SillyGameScene
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.CompassMeta
import org.bukkit.util.Vector
import kotlin.time.Duration.Companion.seconds

class ClueSlot(index: Int, val scene: SillyGameScene, mapData: MapData, private val manager: ClueManager) : Behaviour() {
    var clue: Clue? = null
        private set
    var display: ClueDisplay? = null

    private val button = mapData.positions
        .all("clue_button").withAttribute("index", index).single()
        .resolve(scene.world).noRotation()
    val displayOrigin = mapData.positions
        .all("clue_display").withAttribute("index", index).single()
        .resolve(scene.world).noRotation()

    fun setClue(clue: Clue?) {
        if (clue == this.clue) return

        this.clue?.boundSlot = null
        this.display?.destroy()

        this.clue = clue
        if (clue != null) {
            clue.boundSlot = this
            this.display = ActiveClueDisplay(displayOrigin, clue)
        } else {
            this.display = null
        }
    }

    fun onComplete(player: OfflinePlayer?) {
        setClue(manager.nextClue())
    }

    @Subscribe
    private fun interact(evt: PlayerInteractEvent) {
        val block = evt.clickedBlock ?: return
        if (!block.type.name.endsWith("_BUTTON")) return
        if (clue == null) return
        if (block.location.toBlockLocation() != this.button.toBlockLocation()) return
        evt.isCancelled = true

        if (evt.player.consumeCooldown("sillygame/compass", 5.seconds)) {
            if (evt.player.consumeCooldown("sillygame/compass_warning", 0.5.seconds)) {
                evt.player.error("You're collecting compasses too fast.")
            }
            return
        }

        val compass = clue?.getCompass() ?: return
        val direction = Vector(1.0, 0.0, 0.0)
        val dispenserLocation = block.location.toCenterLocation()
        dispenserLocation.world.dropItem(dispenserLocation, compass) {
            it.velocity = direction.multiply(0.3)

            val dispenser = sound("block.dispenser.dispense")
            it.world.play(dispenser, it.location)
        }
    }

    override fun destroy() {
        super.destroy()
        display?.destroy()
    }
}