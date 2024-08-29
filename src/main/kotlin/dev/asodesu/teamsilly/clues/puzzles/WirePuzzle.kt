package dev.asodesu.teamsilly.clues.puzzles

import dev.asodesu.origami.engine.wiring.annotations.Subscribe
import dev.asodesu.origami.utilities.bukkit.debug
import dev.asodesu.origami.utilities.bukkit.off
import dev.asodesu.origami.utilities.bukkit.runLater
import dev.asodesu.origami.utilities.lerp
import dev.asodesu.origami.utilities.play
import dev.asodesu.origami.utilities.success
import dev.asodesu.origami.utilities.ticks
import dev.asodesu.teamsilly.build.MapData
import dev.asodesu.teamsilly.clues.CluePuzzle
import dev.asodesu.teamsilly.utils.audience
import dev.asodesu.teamsilly.utils.playersInBox
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.type.LightningRod
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

class WirePuzzle(id: String, val world: World, mapData: MapData) : CluePuzzle(id, mapData, world) {
    override val name: String = "Wire"
    private val sides = listOf(arrayOf(1, 0), arrayOf(0, 1), arrayOf(-1, 0), arrayOf(0, -1))
    private val transferableBlocks = listOf(Material.COPPER_BLOCK, Material.WAXED_COPPER_BLOCK, Material.WAXED_CHISELED_COPPER, Material.CHISELED_COPPER, Material.LIGHTNING_ROD)
    private val finishBlocks = listOf(Material.PURPLE_CONCRETE, Material.MAGENTA_CONCRETE, Material.RED_CONCRETE, Material.LIGHT_BLUE_CONCRETE, Material.YELLOW_CONCRETE, Material.LIME_CONCRETE)

    private var lastInteractPlayer: Player? = null
    private var stepCookie = -1
    private var completedBlocks = 0
    private var steppedBlocks = mutableListOf<Location>()
    fun start(block: Block) {
        // generate a random id for this step
        val newCookie = Random.nextInt(0, Short.MAX_VALUE.toInt())
        if (stepCookie == newCookie) {
            debug("we just hit a 1/32767 chance of generating 2 of the same cookie")
            return start(block)
        }

        reset() // reset rods powered state
        stepCookie = newCookie // set new cookie
        completedBlocks = 0 // reset completed blocks
        steppedBlocks = mutableListOf() // reset stepped blocks
        step(block, stepCookie) // step the first block
    }

    fun reset() {
        stepCookie = -1
        steppedBlocks.forEach {
            if (it.block.type != Material.LIGHTNING_ROD) return@forEach
            val data = it.block.blockData as LightningRod
            data.isPowered = false
            it.block.blockData = data
        }
    }

    fun step(block: Block, cookie: Int) {
        if (stepCookie != cookie) return // ensure we haven't reset the step
        if (steppedBlocks.contains(block.location)) return // aso while loop protection:tm::

        if (block.type == Material.LIGHTNING_ROD) { // light up lighting rods
            val data = block.blockData as LightningRod
            data.isPowered = true // i hate bukkit
            block.blockData = data
            runLater(2.seconds) { // un-light up lighting rod
                if (stepCookie != cookie) return@runLater
                data.isPowered = false
                block.blockData = data
            }
        }

        // check for the final blocks
        if (block.type == Material.WAXED_CHISELED_COPPER) {
            // check if this IS a final block and not a rotation point
            val aboveBlock = block.location.off(y = 1).block
            if (finishBlocks.contains(aboveBlock.type)) {
                completedBlocks++

                // send messages
                val players = regions.playersInBox(world)
                val audience = players.audience()
                audience.success("Reached ${aboveBlock.type} receptor <gray>(${completedBlocks}/${finishBlocks.size})")
                audience.play("block.note_block.bit", pitch = lerp(0.8f, 2f, completedBlocks / finishBlocks.size.toFloat()))

                // check for all
                if (completedBlocks >= finishBlocks.size) {
                    val player = players.randomOrNull()
                        ?: lastInteractPlayer
                        ?: world.players.random()
                    this.complete(player)
                }
            }
        }

        // ensure we don't re-step this block
        steppedBlocks += block.location
        // check for transferrable blocks on all sides
        sides.forEach { (x, z) ->
            val offsetBlock = block.off(x, z)
            // if we can't transfer from here, stop.
            if (!transferableBlocks.contains(offsetBlock.type)) return@forEach

            // check for double-transfers through solid blocks
            if (offsetBlock.type != Material.LIGHTNING_ROD && block.type != Material.LIGHTNING_ROD) {
                return@forEach
            }

            // step this block! (later)
            runLater(3.ticks) { step(offsetBlock, cookie) }
        }
    }

    @Subscribe
    fun button(evt: PlayerInteractEvent) {
        val button = evt.clickedBlock ?: return
        val block = button.location.off(y = -1).block
        if (button.type != Material.POLISHED_BLACKSTONE_BUTTON) return
        if (block.type == Material.CUT_COPPER) return start(block)

        if (block.type != Material.WAXED_CHISELED_COPPER) return
        evt.isCancelled = true

        val rotatePosX = block.hasWire(1, 0)
        val rotatePosZ = block.hasWire(0, 1)
        val rotateNegX = block.hasWire(-1, 0)
        val rotateNegZ = block.hasWire(0, -1)

        if (rotatePosX) {
            if (!rotateNegZ) block.off(1, 0).type = Material.AIR
            block.setRod(0, 1, BlockFace.SOUTH)
        }
        if (rotatePosZ) {
            if (!rotatePosX) block.off(0, 1).type = Material.AIR
            block.setRod(-1, 0, BlockFace.WEST)
        }
        if (rotateNegX) {
            if (!rotatePosZ) block.off(-1, 0).type = Material.AIR
            block.setRod(0, -1, BlockFace.NORTH)
        }
        if (rotateNegZ) {
            if (!rotateNegX) block.off(0, -1).type = Material.AIR
            block.setRod(1, 0, BlockFace.EAST)
        }
        lastInteractPlayer = evt.player
    }
    private fun Block.setRod(x: Int, z: Int, facing: BlockFace) {
        off(x, z).blockData = Material.LIGHTNING_ROD.createBlockData {
            (it as LightningRod).facing = facing
        }
    }
    private fun Block.off(x: Int, z: Int) = location.off(x = x, z = z).block
    private fun Block.hasWire(x: Int, z: Int): Boolean {
        return off(x, z).type == Material.LIGHTNING_ROD
    }
}