package dev.asodesu.teamsilly.game

import dev.asodesu.origami.engine.add
import dev.asodesu.origami.engine.scene.OfflinePlayerScene
import dev.asodesu.origami.engine.wiring.annotations.Subscribe
import dev.asodesu.origami.utilities.bukkit.allPlayers
import dev.asodesu.teamsilly.build.MapData
import dev.asodesu.teamsilly.build.element.all
import dev.asodesu.teamsilly.build.element.resolve
import dev.asodesu.teamsilly.clues.Clue
import dev.asodesu.teamsilly.clues.ClueManager
import dev.asodesu.teamsilly.config.Locations
import org.bukkit.Location
import org.bukkit.OfflinePlayer
import org.bukkit.World
import org.spigotmc.event.player.PlayerSpawnLocationEvent

class SillyGameScene(world: World, mapData: MapData, id: String) : OfflinePlayerScene(id) {
    val playerSpawnPositions = mapData.positions.all("player_spawn").resolve(world)
    var avaliableSpawns = playerSpawnPositions.toMutableList()

    override fun init() {
        super.init()
        allPlayers.forEach {
            addPlayer(it)
            it.teleportAsync(getPlayerSpawn())
        }
    }

    override fun setupComponents(player: OfflinePlayer) {
    }

    override fun setupComponents() {
        this.add<ClueManager>()
    }

    @Subscribe
    fun location(evt: PlayerSpawnLocationEvent) {
        evt.spawnLocation = getPlayerSpawn()
    }

    fun getPlayerSpawn(): Location {
        var spawn = avaliableSpawns.randomOrNull()
        if (spawn == null) {
            avaliableSpawns = playerSpawnPositions.toMutableList()
            spawn = avaliableSpawns.random()
        }
        avaliableSpawns.remove(spawn)
        return spawn
    }
}