package dev.asodesu.teamsilly.behaviour

import dev.asodesu.origami.engine.player.PlayerBehaviour
import dev.asodesu.origami.engine.player.PlayerBehaviourContainer
import dev.asodesu.origami.engine.wiring.annotations.Subscribe
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.player.PlayerInteractEvent

class PlayerProtection(c: PlayerBehaviourContainer) : PlayerBehaviour(c) {

    @Subscribe
    fun damage(evt: EntityDamageEvent) {
        if (evt.entity == player) evt.isCancelled = true
    }

    @Subscribe
    fun food(evt: FoodLevelChangeEvent) {
        evt.isCancelled = true
    }

    @Subscribe
    fun blockBreak(evt: BlockBreakEvent) {
        evt.isCancelled = true
    }

    @Subscribe
    fun blockPlace(evt: BlockPlaceEvent) {
        evt.isCancelled = true
    }

    @Subscribe
    fun equip(evt: PlayerInteractEvent) {
        if (evt.item?.type?.name?.endsWith("_HELMET") == true) {
            evt.isCancelled = true
        }
    }

}