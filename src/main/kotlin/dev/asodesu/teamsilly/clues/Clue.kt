package dev.asodesu.teamsilly.clues

import dev.asodesu.origami.engine.add
import dev.asodesu.origami.utilities.miniMessage
import dev.asodesu.origami.utilities.play
import dev.asodesu.origami.utilities.sendTitle
import dev.asodesu.origami.utilities.success
import dev.asodesu.teamsilly.KEY_CLUEID
import dev.asodesu.teamsilly.clues.slots.ClueSlot
import dev.asodesu.teamsilly.game.SillyGameScene
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.CompassMeta
import org.bukkit.persistence.PersistentDataType
import kotlin.time.Duration.Companion.seconds

class Clue(
    val id: String,
    val name: String,
    val description: String,
    val item: ItemStack,
    val puzzle: BaseCluePuzzle
) : BaseCluePuzzle.CompleteListener {
    var boundSlot: ClueSlot? = null
    lateinit var scene: SillyGameScene

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
        this.scene = scene
    }

    fun onComplete(player: OfflinePlayer?) {
        val name = player?.name ?: "Someone on your team"
        scene.success("<dark_green>$name</dark_green> found ${this.name}")
        scene.sendTitle(
            subtitle = "<dark_green><obf>[]</obf></dark_green> <green>${this.name} collected</green> <dark_green><obf>[]</dark_green>",
            stay = 4.seconds
        )
        scene.play("asodesu:sillygame.clue_found")
        boundSlot?.onComplete(player)

        scene.players.forEach {
            val scenePlayer = it.player ?: return@forEach
            scenePlayer.inventory.removeAll { item ->
                item?.persistentDataContainer?.get(KEY_CLUEID, PersistentDataType.STRING) == id
            }
        }
    }

    override fun onPuzzleComplete(player: Player) {
        player.inventory.addItem(getKey())
        player.sendTitle(subtitle = "<aqua><obf>[]</obf></aqua> Obtained <green>Safe Key <red><obf>[]</obf></red>")
    }

    fun getCompass(): ItemStack {
        val item = ItemStack(Material.COMPASS)
        item.editMeta(CompassMeta::class.java) {
            it.persistentDataContainer.set(KEY_CLUEID, PersistentDataType.STRING, id)
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