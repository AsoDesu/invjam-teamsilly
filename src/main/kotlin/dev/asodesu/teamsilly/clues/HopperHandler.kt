package dev.asodesu.teamsilly.clues

import dev.asodesu.origami.engine.Behaviour
import dev.asodesu.origami.engine.get
import dev.asodesu.origami.engine.wiring.annotations.Subscribe
import dev.asodesu.origami.utilities.bukkit.debug
import dev.asodesu.origami.utilities.bukkit.nextTick
import dev.asodesu.teamsilly.KEY_CLUEID
import dev.asodesu.teamsilly.KEY_HOPPER
import dev.asodesu.teamsilly.build.MapData
import dev.asodesu.teamsilly.build.element.single
import dev.asodesu.teamsilly.game.SillyGameScene
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Hopper
import org.bukkit.event.inventory.InventoryPickupItemEvent
import org.bukkit.persistence.PersistentDataType

class HopperHandler(val scene: SillyGameScene, mapData: MapData) : Behaviour() {
    val clueManager = scene.get<ClueManager>()
    val hopperLocation = mapData.positions.single("hopper").resolve(scene.world)

    fun postApply() {
        hopperLocation.block.type = Material.HOPPER
        val state = hopperLocation.block.state as Hopper
        state.persistentDataContainer.set(KEY_HOPPER, PersistentDataType.BOOLEAN, true)
        state.update()
    }

    @Subscribe
    fun pickup(evt: InventoryPickupItemEvent) {
        val hopper = evt.inventory.holder as? Hopper ?: return
        if (!hopper.persistentDataContainer.has(KEY_HOPPER)) return
        evt.isCancelled = true
        val stack = evt.item.itemStack
        val clueId = stack.persistentDataContainer.get(KEY_CLUEID, PersistentDataType.STRING)
            ?: return

        val clue = clueManager.clues.find { it.id == clueId }
            ?: return

        val thrower = evt.item.thrower?.let { Bukkit.getOfflinePlayer(it) }
        clue.onComplete(thrower)
        evt.item.remove()
    }
}