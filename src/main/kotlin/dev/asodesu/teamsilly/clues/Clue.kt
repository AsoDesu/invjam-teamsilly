package dev.asodesu.teamsilly.clues

import dev.asodesu.origami.engine.add
import dev.asodesu.origami.utilities.miniMessage
import dev.asodesu.origami.utilities.sendTitle
import dev.asodesu.teamsilly.KEY_CLUEID
import dev.asodesu.teamsilly.clues.slots.ClueSlot
import dev.asodesu.teamsilly.game.SillyGameScene
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.CompassMeta
import org.bukkit.persistence.PersistentDataType

class Clue(
    val id: String,
    val name: String,
    val description: String,
    val item: ItemStack,
    val puzzle: BaseCluePuzzle
) : BaseCluePuzzle.CompleteListener {
    var boundSlot: ClueSlot? = null

    init {
        puzzle.completeListener = this
        item.editMeta {
            it.persistentDataContainer.set(KEY_CLUEID, PersistentDataType.STRING, id)
            it.itemName(miniMessage(name))
        }
    }

    fun setupComponents(scene: SillyGameScene) {
        puzzle.setupComponents(scene)
        runCatching { scene.add(SafeHandler(id, this, scene)) }
    }

    fun onComplete(player: OfflinePlayer?) = boundSlot?.onComplete(player)

    override fun onPuzzleComplete(player: Player) {
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
            it.persistentDataContainer.set(KEY_CLUEID, PersistentDataType.STRING, id)
            it.setMaxStackSize(1)
            it.itemName(miniMessage("<green>${this.name}</green> Safe Key"))
        }
        return item
    }
}