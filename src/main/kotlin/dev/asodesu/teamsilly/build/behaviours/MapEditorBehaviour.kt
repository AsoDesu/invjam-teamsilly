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
import dev.asodesu.teamsilly.build.element.PositionElement
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
            tempTextDisplay("<red>❌</red>", location, mapEditorId)
        }
        player.success("Added position <dark_green>$mapEditorId</dark_green> to map at ${position.position}")
        tempTextDisplay("<green>⏺</green>", location, mapEditorId)
        madeChanges = true
    }

    private fun tempTextDisplay(line: String, location: Location, id: String) {
        val text = location.world.spawn(location, TextDisplay::class.java) {
            it.text(miniMessage("$line\n$id"))
            it.billboard = Display.Billboard.CENTER
            it.backgroundColor = Color.fromARGB(0, 0, 0, 0)
        }
        runLater(2.seconds) { text.remove() }
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