package dev.asodesu.teamsilly.clues

import dev.asodesu.origami.utilities.miniMessage
import dev.asodesu.origami.utilities.sendTitle
import dev.asodesu.teamsilly.game.SillyGameScene
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.CompassMeta
import kotlin.time.Duration.Companion.seconds

class Clue(
    val id: String,
    val name: String,
    val item: ItemStack,
    val puzzle: BaseCluePuzzle
) : BaseCluePuzzle.CompleteListener {

    init {
        puzzle.completeListener = this
    }

    fun setupComponents(scene: SillyGameScene) {
        puzzle.setupComponents(scene)
    }

    override fun onComplete(player: Player) {
        player.inventory.addItem(getKey())
        player.sendTitle(subtitle = "<aqua><obf>[]</obf></aqua> Obtained <green>Safe Key <red><obf>[]</obf></red>")
    }

    fun getCompass(): ItemStack {
        val item = ItemStack(Material.COMPASS)
        item.editMeta(CompassMeta::class.java) {
            it.lodestone = this.puzzle.startingLocation
            it.isLodestoneTracked = false
            it.setEnchantmentGlintOverride(false)
            it.setMaxStackSize(1)
            it.itemName(miniMessage("<aqua>${this.name}</aqua> Compass"))
        }
        return item
    }

    fun getKey(): ItemStack {
        val item = ItemStack(Material.TRIAL_KEY)
        item.editMeta {
            it.setMaxStackSize(1)
            it.itemName(miniMessage("<green>${this.name}</green> Safe Key"))
        }
        return item
    }
}