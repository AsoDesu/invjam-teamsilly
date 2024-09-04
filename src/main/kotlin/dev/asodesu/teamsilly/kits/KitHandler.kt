package dev.asodesu.teamsilly.kits

import dev.asodesu.origami.engine.Behaviour
import dev.asodesu.origami.engine.addBy
import dev.asodesu.origami.engine.has
import dev.asodesu.origami.engine.player.container
import dev.asodesu.origami.engine.wiring.annotations.Subscribe
import dev.asodesu.origami.utilities.error
import dev.asodesu.origami.utilities.miniMessage
import dev.asodesu.origami.utilities.play
import dev.asodesu.origami.utilities.success
import dev.asodesu.teamsilly.behaviour.KitBehaviour
import dev.asodesu.teamsilly.build.MapData
import dev.asodesu.teamsilly.build.element.all
import dev.asodesu.teamsilly.build.element.resolve
import dev.asodesu.teamsilly.game.SillyGameScene
import org.bukkit.NamespacedKey
import org.bukkit.World
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.persistence.PersistentDataType

class KitHandler(val scene: SillyGameScene, mapData: MapData) : Behaviour() {
    val standKey = NamespacedKey("asodesu", "kit_stand")

    val stands = mapData.positions.all("kit_stand").resolve(scene.world)
    val kits = Kits()

    init {
        val standLocations = stands.toMutableList()
        kits.all.forEach { (key, kit) ->
            val loc = standLocations.removeFirst()
            loc.world.spawn(loc, ArmorStand::class.java) { stand ->
                kit.stand = stand
                kit.equipment.forEach { stand.equipment.setItem(it.key, it.value) }
                kit.slots.forEach {
                    if (it.key == 0) stand.equipment.setItem(EquipmentSlot.HAND, it.value)
                }
                stand.persistentDataContainer.set(standKey, PersistentDataType.STRING, key)
                stand.setArms(true)

                stand.customName(miniMessage("<dark_green><b>${kit.name}"))
                stand.isCustomNameVisible = true
            }
        }
    }

    @Subscribe
    fun interact(evt: PlayerInteractAtEntityEvent) {
        val player = evt.player
        val entity = evt.rightClicked
        if (entity.type != EntityType.ARMOR_STAND) return

        val kitKey = entity.persistentDataContainer.get(standKey, PersistentDataType.STRING) ?: return
        val kit = kits.all[kitKey] ?: return
        evt.isCancelled = true

        val container = player.container
        if (container.has<KitBehaviour>()) {
            player.error("You already have a kit!")
            return
        }

        kit.equipment.forEach { player.equipment.setItem(it.key, it.value) }
        kit.slots.forEach { player.inventory.setItem(it.key, it.value) }
        kit.behaviour?.let { container.add(it, scope = this.scope) }
        kit.sound.forEach { player.play(it) }
        kit.equippedBy = player.uniqueId

        container.addBy(scope = scene) { KitBehaviour(it, kit) }

        scene.success("${player.name} has equipped <dark_green>${kit.name}")

        entity.remove()
    }

    @Subscribe
    fun destory(evt: EntityDeathEvent) {
        if (evt.entityType == EntityType.ARMOR_STAND) evt.isCancelled = true
    }

    override fun destroy() {
        super.destroy()
        kits.all.forEach { (_, kit) -> kit.stand?.remove() }
    }
}