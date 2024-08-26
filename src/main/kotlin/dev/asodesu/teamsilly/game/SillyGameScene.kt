package dev.asodesu.teamsilly.game

import dev.asodesu.origami.engine.add
import dev.asodesu.origami.engine.addBy
import dev.asodesu.origami.engine.scene.OfflinePlayerScene
import dev.asodesu.origami.engine.wiring.annotations.Subscribe
import dev.asodesu.origami.utilities.bukkit.allPlayers
import dev.asodesu.teamsilly.build.MapData
import dev.asodesu.teamsilly.build.element.all
import dev.asodesu.teamsilly.build.element.resolve
import dev.asodesu.teamsilly.clues.Clue
import dev.asodesu.teamsilly.clues.ClueManager
import dev.asodesu.teamsilly.clues.HopperHandler
import dev.asodesu.teamsilly.clues.SafeHandler
import dev.asodesu.teamsilly.config.Locations
import dev.asodesu.teamsilly.utils.reset
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.audience.ForwardingAudience
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.OfflinePlayer
import org.bukkit.World
import org.bukkit.entity.Player
import org.spigotmc.event.player.PlayerSpawnLocationEvent

class SillyGameScene(val world: World, val mapData: MapData, id: String) : OfflinePlayerScene(id), ForwardingAudience {
    val playerSpawnPositions = mapData.positions.all("player_spawn").resolve(world)
    var avaliableSpawns = playerSpawnPositions.toMutableList()

    override fun init() {
        super.init()
        allPlayers.forEach {
            addPlayer(it)
            it.teleportAsync(getPlayerSpawn())
            it.reset()
            it.gameMode = GameMode.SURVIVAL
        }
    }

    override fun setupComponents(player: OfflinePlayer) {
    }

    override fun setupComponents() {
        this.add(ClueManager(this, mapData))
        this.add(HopperHandler(this, mapData))
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

    override fun audiences() = players.mapNotNull { it.player }
}