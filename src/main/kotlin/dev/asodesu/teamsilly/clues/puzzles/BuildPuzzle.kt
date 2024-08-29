package dev.asodesu.teamsilly.clues.puzzles

import dev.asodesu.origami.engine.wiring.annotations.Subscribe
import dev.asodesu.origami.engine.wiring.annotations.Tick
import dev.asodesu.origami.utilities.bukkit.debug
import dev.asodesu.origami.utilities.lerp
import dev.asodesu.origami.utilities.play
import dev.asodesu.origami.utilities.success
import dev.asodesu.teamsilly.build.MapData
import dev.asodesu.teamsilly.build.element.all
import dev.asodesu.teamsilly.build.element.withAttribute
import dev.asodesu.teamsilly.clues.CluePuzzle
import dev.asodesu.teamsilly.utils.audience
import dev.asodesu.teamsilly.utils.playersInBox
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.Chest
import org.bukkit.block.data.Directional
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.util.BoundingBox
import kotlin.math.ceil

class BuildPuzzle(id: String, val world: World, mapData: MapData) : CluePuzzle(id, mapData, world) {
    override val name: String = "Build"
    val builds = getBuilds(mapData)
    var totalCompleted = 0

    init {
        val chests = mapData.positions.all("build_chest").withPuzzleId()
        val items = builds.flatMap { it.items }
        val perChest = ceil(items.size.toFloat() / chests.size).toInt()
        debug("${items.size} / ${chests.size} / $perChest")

        var i = 0
        chests.forEach { pos ->
            val block = pos.resolve(world).block
            val direction = BlockFace.valueOf((pos.attributes["facing"] ?: "west").uppercase())

            block.blockData = Material.CHEST.createBlockData {
                (it as Directional).facing = direction
            }
            val chest = block.state as Chest
            chest.inventory.clear()

            val slots = MutableList(chest.inventory.size) { it }
            for (t in 0 until perChest) {
                val item = items.getOrNull(i) ?: continue
                val slot = slots.random()
                chest.inventory.setItem(slot, item)
                slots.remove(slot)
                i++
            }
        }
    }

    var dirtyCheck = false
    var lastPlayer: Player? = null

    @Tick
    fun check() {
        if (!dirtyCheck) return
        dirtyCheck = false

        builds.forEach {
            if (it.completed) return@forEach
            if (!it.checkCompleted()) return@forEach

            it.completed = true
            totalCompleted++

            val audience = regions.playersInBox(world).audience()
            audience.success("Completed Build #${it.number} <gray>($totalCompleted/${builds.size})")
            audience.play("asodesu:sillygame.generic.alert", pitch = lerp(0.9f, 1.15f, (totalCompleted.toFloat() / builds.size)))
        }

        if (totalCompleted >= builds.size) {
            complete(lastPlayer!!)
        }
    }

    @Subscribe
    fun blockPlace(evt: BlockPlaceEvent) {
        val inBound = builds.any { it.checkRegion.contains(evt.block.location.toVector()) }
        if (!inBound) return
        dirtyCheck = true
        lastPlayer = evt.player
    }

    fun getBuilds(mapData: MapData): List<Build> {
        val buildCopyRegions = mapData.boundingBoxes.all("build_copy").withPuzzleId()
        val buildCheckRegions = mapData.boundingBoxes.all("build_region").withPuzzleId()

        return buildCopyRegions.mapIndexed { i, copyRegion ->
            val checkRegion = buildCheckRegions.withAttribute("build", i).single().box
            Build(i + 1, copyRegion.box, checkRegion, world)
        }
    }

    class Build(val number: Int, copyRegion: BoundingBox, val checkRegion: BoundingBox, val world: World) {
        private val copyBlocks = getMaterials(copyRegion)
        val items = getItems(copyRegion)
        var completed: Boolean = false

        init {
            clear(checkRegion)
        }

        fun checkCompleted(): Boolean {
            val blocks = getMaterials(checkRegion)
            var i = 0
            return copyBlocks.all { blocks[i++] == it }
        }

        fun getMaterials(region: BoundingBox) = getBlocks(region).map { it.type }
        fun clear(region: BoundingBox) = getBlocks(region).forEach { it.type = Material.AIR }

        fun getBlocks(region: BoundingBox): Array<Block> {
            val min = region.min.toBlockVector()
            val max = region.max.toBlockVector()
            val list = mutableListOf<Block>()
            for (x in min.blockX until max.blockX) {
                for (y in min.blockY until max.blockY) {
                    for (z in min.blockZ until max.blockZ) {
                        list += world.getBlockAt(x,y,z)
                    }
                }
            }
            return list.toTypedArray()
        }

        fun getItems(region: BoundingBox): List<ItemStack> {
            val counts = mutableMapOf<Material, Int>()
            getMaterials(region).forEach {
                counts[it] = (counts[it] ?: 0) + 1
            }
            return counts.map { ItemStack(it.key, it.value) }
        }
    }
}