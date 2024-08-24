package dev.asodesu.teamsilly.clues.slots

import dev.asodesu.origami.utilities.miniMessage
import dev.asodesu.teamsilly.clues.Clue
import dev.asodesu.teamsilly.config.Locations
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.CompassMeta

class ClueSlot(private val index: Int) {
    private var clue: Clue? = null
    val button = Locations.clueButtons[index].block

    fun getCompass(): ItemStack? {
        val clue = clue ?: return null

        val item = ItemStack(Material.COMPASS)
        item.editMeta(CompassMeta::class.java) {
            it.lodestone = clue.compassTarget
            it.isLodestoneTracked = false
            it.setEnchantmentGlintOverride(false)
            it.setMaxStackSize(1)
            it.itemName(miniMessage("${clue.name} Compass"))
        }
        return item
    }


    fun setClue(clue: Clue?) {
        this.clue = clue
    }
}