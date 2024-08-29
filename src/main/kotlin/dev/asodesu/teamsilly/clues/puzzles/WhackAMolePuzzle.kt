package dev.asodesu.teamsilly.clues.puzzles

import dev.asodesu.origami.engine.Destroyable
import dev.asodesu.origami.engine.consumeCooldown
import dev.asodesu.origami.engine.wiring.annotations.Subscribe
import dev.asodesu.origami.utilities.bukkit.loop
import dev.asodesu.origami.utilities.bukkit.off
import dev.asodesu.origami.utilities.bukkit.runLater
import dev.asodesu.origami.utilities.lerp
import dev.asodesu.origami.utilities.play
import dev.asodesu.origami.utilities.success
import dev.asodesu.origami.utilities.ticks
import dev.asodesu.origami.utilities.warning
import dev.asodesu.teamsilly.build.MapData
import dev.asodesu.teamsilly.build.element.all
import dev.asodesu.teamsilly.build.element.noRotation
import dev.asodesu.teamsilly.build.element.resolve
import dev.asodesu.teamsilly.clues.CluePuzzle
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitTask
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class WhackAMolePuzzle(id: String, val world: World, mapData: MapData) : CluePuzzle(id, mapData, world) {
    override val name = "Whack-a-mole"
    val startButton = mapData.positions.all("whack_start").withPuzzleId().resolve(world).map { it.noRotation() }
    val moles = mapData.positions.all("whack_mole").withPuzzleId().resolve(world)

    var running: Instance? = null
    fun start() {
        running?.destroy()
        running = Instance(moles, this)
    }

    override fun destroy() {
        super.destroy()
        running?.destroy()
    }

    class Instance(val moles: List<Location>, val puzzle: WhackAMolePuzzle) : Destroyable {
        var done = 0
        var total = 20

        var lastMole: Location? = null
        var mole: Mole? = null

        init {
            newMole()
        }

        fun newMole() {
            val moleLocation = moles.random()
            if (moleLocation == lastMole) return newMole()

            mole = Mole(moleLocation)
            lastMole = moleLocation
        }

        fun hit(evt: EntityDamageByEntityEvent) {
            if (evt.entity.entityId != mole?.stand?.entityId) return
            evt.isCancelled = true
            if (evt.damager !is Player) return

            val damager = evt.damager as Player
            mole?.retreat()
            done++

            damager.play("block.note_block.bit", pitch = lerp(0.8f, 2f, done / total.toFloat()))
            if (done >= total) puzzle.complete(damager)
            else {
                runLater(0.3.seconds) { newMole() }
            }
        }

        override fun destroy() {
            val mole = mole ?: return
            if (mole.isValid && mole.state != 2) {
                mole.retreat()
            }
        }
    }

    class Mole(val location: Location) : Destroyable {
        private val downLocation = location.off(y = -1.25)
        val stand = location.world.spawn(downLocation, ArmorStand::class.java) {
            it.equipment.helmet = ItemStack(Material.PIGLIN_HEAD)
            it.setGravity(false)
        }
        val isValid get() = stand.isValid

        var task: BukkitTask? = null
        var state = 1 // 0 - poping up, 1 - awaiting hit, 2 - retreating

        init {
            popUp()
        }

        fun popUp() {
            state = 0
            interpolate(0.5.seconds, stand.location, location)
        }

        fun retreat() {
            state = 2
            interpolate(0.5.seconds, stand.location, downLocation) {
                stand.remove()
            }
        }

        fun interpolate(time: Duration, start: Location, end: Location, done: () -> Unit = {}) {
            val ticks = time.ticks
            if (task?.isCancelled == false) task?.cancel()
            task = loop(ticks, 1.ticks) {
                if (!stand.isValid) return@loop

                val t = this.i / this.total.toDouble()
                val location = location.clone().also {
                    it.y = lerp(start.y, end.y, t)
                }
                stand.teleport(location)
                if (remaining == 1) done()
            }
        }

        override fun destroy() {
            stand.remove()
            task?.cancel()
        }
    }

    @Subscribe
    fun interact(evt: PlayerInteractEvent) {
        val block = evt.clickedBlock ?: return
        if (evt.player.consumeCooldown("whack_a_mole/start", 5.ticks)) return

        val isStartButton = startButton.any { it.toBlockLocation() == block.location.toBlockLocation() }
        if (isStartButton) {
            if (running == null) evt.player.success("Starting Whack-a-mole!")
            else evt.player.warning("Restarting Whack-a-mole!")
            start()
        }
    }

    @Subscribe
    fun hit(evt: EntityDamageByEntityEvent) {
        running?.hit(evt)
    }
}