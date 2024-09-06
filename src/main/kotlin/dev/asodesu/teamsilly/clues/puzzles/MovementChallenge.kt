package dev.asodesu.teamsilly.clues.puzzles

import dev.asodesu.origami.engine.get
import dev.asodesu.origami.engine.getOrNull
import dev.asodesu.origami.engine.player.container
import dev.asodesu.origami.engine.wiring.annotations.Subscribe
import dev.asodesu.origami.utilities.error
import dev.asodesu.origami.utilities.warning
import dev.asodesu.teamsilly.behaviour.KitBehaviour
import dev.asodesu.teamsilly.build.MapData
import dev.asodesu.teamsilly.build.element.all
import dev.asodesu.teamsilly.clues.CluePuzzle
import dev.asodesu.teamsilly.utils.isSpectating
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack

open class MovementChallenge(
    id: String,
    override val name: String,
    val world: World,
    mapData: MapData,
    override val kitRequirement: String = "Any"
) : CluePuzzle(id, mapData, world) {
    private val finishRegion = mapData.boundingBoxes.all("puzzle_finish").withPuzzleId().single().box

    override fun enter(player: Player) {
        super.enter(player)
        if (name == "Parkour" || name == "Trident") {
            val kitId = player.container.getOrNull<KitBehaviour>()?.kit?.id
            if (kitId == "winds") {
                player.warning("Your wind charges have been removed in the parkour challenge")
                player.inventory.remove(Material.WIND_CHARGE)
            } else if (kitId == "elytra") {
                player.warning("Your elytra has been removed in the parkour challenge")
                player.inventory.remove(Material.ELYTRA)
            }
        }
    }

    override fun exit(player: Player) {
        super.exit(player)
        if (name == "Parkour" || name == "Trident") {
            val kitId = player.container.getOrNull<KitBehaviour>()?.kit?.id
            if (kitId == "winds") {
                player.warning("Your wind charges have been returned")
                player.inventory.addItem(ItemStack(Material.WIND_CHARGE, 64))
            } else if (kitId == "elytra") {
                player.warning("Your elytra has been returned")
                player.inventory.setItem(EquipmentSlot.CHEST, ItemStack(Material.ELYTRA).also { it.editMeta { meta -> meta.isUnbreakable = true } })
            }
        }
    }

    @Subscribe
    fun move(evt: PlayerMoveEvent) {
        if (this.isCompleted) return
        if (evt.player.isSpectating) return
        if (finishRegion.contains(evt.to.toVector())) {
            this.complete(evt.player)
        }
    }
}