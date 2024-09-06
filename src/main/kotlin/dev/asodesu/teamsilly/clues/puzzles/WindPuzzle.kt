package dev.asodesu.teamsilly.clues.puzzles

import dev.asodesu.origami.engine.wiring.annotations.Subscribe
import dev.asodesu.origami.utilities.bukkit.debug
import dev.asodesu.origami.utilities.bukkit.off
import dev.asodesu.origami.utilities.bukkit.play
import dev.asodesu.origami.utilities.bukkit.runLater
import dev.asodesu.origami.utilities.lerp
import dev.asodesu.origami.utilities.sound
import dev.asodesu.origami.utilities.success
import dev.asodesu.origami.utilities.ticks
import dev.asodesu.teamsilly.build.MapData
import dev.asodesu.teamsilly.build.element.all
import dev.asodesu.teamsilly.build.element.noRotation
import dev.asodesu.teamsilly.build.element.resolve
import dev.asodesu.teamsilly.build.element.resolveSingle
import dev.asodesu.teamsilly.clues.CluePuzzle
import io.papermc.paper.event.block.TargetHitEvent
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.sound.Sound
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.data.Lightable
import org.bukkit.entity.Player
import kotlin.random.Random
import kotlin.random.nextInt
import kotlin.time.Duration.Companion.seconds

class WindPuzzle(id: String, val world: World, mapData: MapData) : CluePuzzle(id, mapData, world) {
    override val name: String = "Simon Says"
    override val kitRequirement: String = "Wind Charges"

    val removeBlocks = mapData.boundingBoxes.all("remove_blocks").withPuzzleId().resolveSingle(world)
    val triggers = mapData.positions.all("wind_trigger").withPuzzleId().resolve(world).map { it.toBlockLocation().noRotation() }

    val sequence = List(6) { Random.nextInt(triggers.indices) }
    var instance: Instance? = null

    init {
        triggers.forEach {
            it.block.type = Material.TARGET
            it.off(y = 1).block.type = Material.REDSTONE_LAMP
        }
    }

    fun start() {
        instance?.canceled = true
        instance = null

        val new = Instance(this)
        instance = new
    }

    override fun complete(player: Player) {
        super.complete(player)
        for (x in removeBlocks.minX.toInt()..removeBlocks.maxX.toInt()) {
            for (y in removeBlocks.minY.toInt()..removeBlocks.maxY.toInt()) {
                for (z in removeBlocks.minZ.toInt()..removeBlocks.maxZ.toInt()) {
                    val blockAt = world.getBlockAt(x, y, z)
                    if (blockAt.type == Material.NETHERITE_BLOCK) blockAt.type = Material.AIR
                }
            }
        }
    }

    @Subscribe
    fun power(evt: TargetHitEvent) {
        val block = evt.hitBlock?.location?.toBlockLocation()?.noRotation() ?: return

        val index = triggers.indexOfFirst { block == it }
        if (index == -1) return

        evt.isCancelled = true
        if (instance == null) {
            start()
            (evt.entity.shooter as? Audience)?.success("Started Simon Says!")
        } else {
            instance?.hit(index, block, (evt.entity.shooter as? Player) ?: return)
        }
    }


    class Instance(val puzzle: WindPuzzle) {
        var progress = 1
        var state = 0
        var canceled = false

        init {
            later(20) { say() }
        }

        fun say() {
            state = 1

            var ticks = 0
            repeat(progress) {
                val seq = puzzle.sequence[it]
                later(ticks) {
                    light(seq)

                    if (it == progress-1) {
                        copy()
                    }
                }
                ticks += 15
            }
        }

        fun copy() {
            state = 2
            sequenceIndex = 0
        }

        fun later(time: Int, func: () -> Unit) {
            runLater(time.ticks) {
                if (canceled) return@runLater
                func()
            }
        }

        fun light(
            index: Int,
            sound: Sound? = sound("block.note_block.pling", pitch = lerp(0.8f, 2.0f, (index / puzzle.triggers.size.toFloat())))
        ) {
            val light = puzzle.triggers[index].off(y = 1).block

            val data = light.blockData as Lightable
            data.isLit = true
            light.blockData = data
            if (sound != null) light.world.play(sound, light.location)

            later(10) {
                val data2 = light.blockData as Lightable
                data2.isLit = false
                light.blockData = data2
            }
        }

        var sequenceIndex = 0
        fun hit(targetIndex: Int, hitLoc: Location, player: Player) {
            if (state != 2) return

            if (puzzle.sequence[sequenceIndex++] == targetIndex) {
                light(targetIndex)
                if (sequenceIndex == progress) {
                    state = 0
                    progress++
                    if (progress >= puzzle.sequence.size) {
                        puzzle.complete(player)
                    } else {
                        later(20) { say() }
                    }
                }
            } else {
                state = 3
                sequenceIndex = 0
                puzzle.triggers.forEachIndexed { i, _ -> light(i, null) }
                hitLoc.world.play(sound("block.note_block.pling", pitch = 0f), hitLoc)

                runLater(2.seconds) { puzzle.start() }
            }
        }
    }
}