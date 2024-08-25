package dev.asodesu.teamsilly.build.behaviours

import dev.asodesu.origami.engine.player.OnlinePlayerBehaviour
import dev.asodesu.origami.engine.player.PlayerBehaviourContainer
import dev.asodesu.origami.engine.wiring.annotations.Subscribe
import dev.asodesu.origami.utilities.bukkit.runLater
import dev.asodesu.origami.utilities.error
import dev.asodesu.origami.utilities.miniMessage
import dev.asodesu.origami.utilities.success
import dev.asodesu.origami.utilities.warning
import dev.asodesu.teamsilly.build.MapData
import dev.asodesu.teamsilly.build.MapDataHandlers
import dev.asodesu.teamsilly.build.element.BoundingBoxElement
import dev.asodesu.teamsilly.build.element.PositionElement
import dev.asodesu.teamsilly.utils.glowingDisplay
import net.kyori.adventure.bossbar.BossBar
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.entity.Display
import org.bukkit.entity.EntityType
import org.bukkit.entity.TextDisplay
import org.bukkit.event.entity.EntityPlaceEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.util.BoundingBox
import kotlin.time.Duration.Companion.seconds

class MapEditorBehaviour(val map: MapData, val name: String, c: PlayerBehaviourContainer) : OnlinePlayerBehaviour(c) {
    private val bossBar = BossBar.bossBar(miniMessage("<gray>Editing Map - <origami>$name"), 1f, BossBar.Color.WHITE, BossBar.Overlay.PROGRESS)
    private val undoQueue = mutableListOf<() -> Unit>()
    private var madeChanges: Boolean = false
        set(value) {
            if (field != value) {
                if (value) bossBar.progress(0f)
                else bossBar.progress(1f)
            }
            field = value
        }

    fun save() {
        MapDataHandlers.save(name, map)
        madeChanges = false
    }

    private fun postApply() {
        player.showBossBar(bossBar)
    }

    @Subscribe
    private fun join(evt: PlayerJoinEvent) = postApply()

    @Subscribe(ignoreFilter = true)
    private fun place(evt: EntityPlaceEvent) {
        val player = evt.player ?: return
        if (player != this.player) return
        if (evt.entityType != EntityType.ARMOR_STAND) return

        val item = player.inventory.getItem(evt.hand)
        if (item.mapEditorId == null) return
        evt.isCancelled = true

        val loc = evt.entity.location
        if (evt.block.location.y > loc.y) {
            loc.y = evt.block.location.toCenterLocation().y
        }
        addPosition(item, loc)
    }

    @Subscribe
    fun interact(evt: PlayerInteractEvent) {
        if (!evt.action.isLeftClick) return

        val item = evt.item ?: return
        if (item.mapEditorId != null)
            addPosition(item, player.location)
    }

    private fun addPosition(item: ItemStack, location: Location) {
        val mapEditorId = item.mapEditorId ?: return
        val attributes = item.mapEditorAttributes

        val position = PositionElement(mapEditorId, location, attributes)
        map.positions.add(position)
        undoQueue.addFirst {
            map.positions.remove(position)
            player.warning("Removed position <dark_green>$mapEditorId</dark_green> from map at ${position.position}")
            highlightPosition("<red>❌</red>", location, mapEditorId)
        }
        player.success("Added position <dark_green>$mapEditorId</dark_green> to map at ${position.position}")
        highlightPosition("<green>⏺</green>", location, mapEditorId)
        madeChanges = true
    }

    fun addBoundingBox(box: BoundingBox, id: String, attributes: Map<String, String>) {
        val element = BoundingBoxElement(id, box, attributes)
        map.boundingBoxes.add(element)
        highlightBound(Color.GREEN, box)
        undoQueue.addFirst {
            highlightBound(Color.RED, box)
            map.boundingBoxes.remove(element)
            player.warning("Removed box <dark_green>$id</dark_green> from map")
        }
        player.success("Added box <dark_green>$id</dark_green> to map")
        madeChanges = true
    }

    fun highlightPosition(line: String, location: Location, id: String) {
        val text = location.world.spawn(location, TextDisplay::class.java) {
            it.text(miniMessage("$line\n$id"))
            it.billboard = Display.Billboard.CENTER
            it.backgroundColor = Color.fromARGB(0, 0, 0, 0)
            it.isSeeThrough = true
        }
        runLater(2.seconds) { text.remove() }
    }

    fun highlightBound(color: Color, boundingBox: BoundingBox) {
        val glow = boundingBox.glowingDisplay(player.world, color)
        runLater(2.seconds) { glow.remove() }
    }

    fun undo() {
        val first = undoQueue.removeFirstOrNull()
            ?: return player.error("Nothing left to undo!")
        first.invoke()
    }

    private fun postRemove() {
        playerOrNull?.hideBossBar(bossBar)
    }
}