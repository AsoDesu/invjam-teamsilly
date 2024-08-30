package dev.asodesu.teamsilly.clues.puzzles

import dev.asodesu.origami.engine.wiring.annotations.Subscribe
import dev.asodesu.origami.utilities.play
import dev.asodesu.teamsilly.KEY_CLUEID
import dev.asodesu.teamsilly.build.MapData
import dev.asodesu.teamsilly.clues.CluePuzzle
import dev.asodesu.teamsilly.utils.SOUND_PUZZLES_FINISH
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.persistence.PersistentDataType

class SearchPuzzle(id: String, val world: World, mapData: MapData) : CluePuzzle(id, mapData, world) {
    override val name = "Search"

    override fun getSubtitle(): String {
        return "<aqua><obf>[]</obf></aqua> Find the key <red><obf>[]</obf>"
    }

    @Subscribe
    fun takeKey(evt: InventoryClickEvent) {
        val player = evt.whoClicked as? Player ?: return
        if (evt.clickedInventory == player.inventory) return

        val item = evt.currentItem ?: return
        if (item.type != Material.TRIAL_KEY) return
        if (item.persistentDataContainer.get(KEY_CLUEID, PersistentDataType.STRING) != id) return

        isCompleted = true
        player.play(SOUND_PUZZLES_FINISH)
        this.destroy()
    }
}