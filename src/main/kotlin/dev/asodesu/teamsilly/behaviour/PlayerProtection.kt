package dev.asodesu.teamsilly.behaviour

import dev.asodesu.origami.engine.player.PlayerBehaviour
import dev.asodesu.origami.engine.player.PlayerBehaviourContainer
import dev.asodesu.origami.engine.wiring.annotations.Subscribe
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.player.PlayerInteractEvent

class PlayerProtection(c: PlayerBehaviourContainer) : PlayerBehaviour(c) {

    @Subscribe(ignoreFilter = true)
    fun damage(evt: EntityDamageEvent) {
        if (evt.entity.uniqueId == offlinePlayer.uniqueId) evt.isCancelled = true
    }

    @Subscribe
    fun byEntity(evt: EntityDamageByEntityEvent) {
        if (evt.entity.type == EntityType.ARMOR_STAND) evt.isCancelled = true
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

    @Subscribe
    fun interact(evt: PlayerInteractAtEntityEvent) {
        if (evt.rightClicked.type == EntityType.ARMOR_STAND || evt.rightClicked.type == EntityType.ITEM_FRAME)
            evt.isCancelled = true
    }

    @Subscribe
    fun inventoryClick(evt: InventoryClickEvent) {
        if (evt.clickedInventory != evt.whoClicked.inventory) return
        if (evt.slot == 38) {
            evt.isCancelled = true
        }
    }

    @Subscribe
    fun drop(evt: PlayerDropItemEvent) {
        val item = evt.itemDrop.itemStack
        if (item.type == Material.TRIDENT || item.type == Material.WIND_CHARGE || item.type == Material.ELYTRA) {
            evt.isCancelled = true
        }
    }

}