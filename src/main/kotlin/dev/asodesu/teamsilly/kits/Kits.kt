package dev.asodesu.teamsilly.kits

import dev.asodesu.origami.engine.Behaviour
import java.util.UUID
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.ArmorStand
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import kotlin.reflect.KClass

class Kits {
    val all = mutableMapOf<String, Kit>()

    val elytra = kit(
        id = "elytra",
        name = "Elytra",
        equipment = mapOf(
            EquipmentSlot.CHEST to ItemStack(Material.ELYTRA).also { it.editMeta { meta -> meta.isUnbreakable = true } }
        ),
        sound = listOf("item.armor.equip_elytra")
    )
    val riptide = kit(
        id = "riptide",
        name = "Riptide",
        slots = mapOf(
            0 to ItemStack(Material.TRIDENT).also { it.editMeta { meta ->
                meta.isUnbreakable = true
                meta.addEnchant(Enchantment.RIPTIDE, 1, false)
            } }
        ),
        sound = listOf("item.armor.equip_generic", "item.trident.riptide_1")
    )
    val windCharges = kit(
        id = "winds",
        name = "Wind Charges",
        behaviour = WindChargeBehaviour::class,
        slots = mapOf(
            0 to ItemStack(Material.WIND_CHARGE, 64)
        ),
        sound = listOf("item.armor.equip_generic", "entity.wind_charge.wind_burst")
    )
    val speed = kit(
        id = "speed",
        name = "Speed",
        behaviour = SpeedBehaviour::class
    )

    fun kit(id: String, name: String, behaviour: KClass<out Behaviour>? = null, equipment: Map<EquipmentSlot, ItemStack> = mapOf(), slots: Map<Int, ItemStack> = mapOf(), sound: List<String> = listOf("item.armor.equip_generic")): Kit {
        val kit = Kit(id, name, behaviour, equipment, slots, sound)
        all[kit.id] = kit
        return kit
    }
    class Kit(
        val id: String,
        val name: String,
        val behaviour: KClass<out Behaviour>? = null,
        val equipment: Map<EquipmentSlot, ItemStack> = mapOf(),
        val slots: Map<Int, ItemStack> = mapOf(),
        val sound: List<String>,
    ) {
        var equippedBy: UUID? = null
        var stand: ArmorStand? = null
    }
}