package dev.asodesu.teamsilly.kits

import dev.asodesu.origami.engine.player.PlayerBehaviour
import dev.asodesu.origami.engine.player.PlayerBehaviourContainer
import dev.asodesu.origami.engine.wiring.annotations.Subscribe
import dev.asodesu.origami.utilities.bukkit.nextTick
import org.bukkit.Material
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

class WindChargeBehaviour(c: PlayerBehaviourContainer) : PlayerBehaviour(c) {

    @Subscribe
    fun use(evt: PlayerInteractEvent) {
        val item = evt.item ?: return
        if (item.type != Material.WIND_CHARGE) return
        nextTick { item.amount = 64 }
    }

}