package dev.asodesu.teamsilly.clues.puzzles

import dev.asodesu.origami.engine.player.OnlinePlayerBehaviour
import dev.asodesu.origami.engine.wiring.annotations.Subscribe
import dev.asodesu.origami.utilities.error
import dev.asodesu.teamsilly.build.MapData
import dev.asodesu.teamsilly.build.element.all
import dev.asodesu.teamsilly.build.element.resolveSingle
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityToggleGlideEvent

class ElytraChallenge(id: String, world: World, mapData: MapData,) : MovementChallenge(id, "Elytra", world, mapData, "Elytra") {
    val respawnLocation = mapData.positions.all("elytra_respawn").withPuzzleId().resolveSingle(world)

    @Subscribe
    fun stopGliding(evt: EntityToggleGlideEvent) {
        if (evt.entity !is Player) return
        if (!isInRegion(evt.entity.location)) return
        if (!evt.isGliding) respawn(evt.entity as Player)
    }

    @Subscribe
    fun fall(evt: EntityDamageEvent) {
        if (evt.entity !is Player) return
        if (evt.cause != EntityDamageEvent.DamageCause.FALL) return
        if (!isInRegion(evt.entity.location)) return

        respawn(evt.entity as Player)
    }

    fun respawn(player: Player) {
        player.teleport(respawnLocation)
        player.error("You fell, returned to top.")
    }

}