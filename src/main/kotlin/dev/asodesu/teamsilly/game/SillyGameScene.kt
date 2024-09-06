package dev.asodesu.teamsilly.game

import dev.asodesu.origami.engine.add
import dev.asodesu.origami.engine.player.container
import dev.asodesu.origami.engine.scene.OfflinePlayerScene
import dev.asodesu.origami.engine.wiring.annotations.Subscribe
import dev.asodesu.origami.utilities.bukkit.filterUUIDs
import dev.asodesu.origami.utilities.bukkit.filterWorld
import dev.asodesu.teamsilly.behaviour.PlayerProtection
import dev.asodesu.teamsilly.build.MapData
import dev.asodesu.teamsilly.build.element.all
import dev.asodesu.teamsilly.build.element.resolve
import dev.asodesu.teamsilly.clues.ClueManager
import dev.asodesu.teamsilly.clues.HopperHandler
import dev.asodesu.teamsilly.kits.KitHandler
import dev.asodesu.teamsilly.utils.reset
import java.util.*
import net.kyori.adventure.audience.ForwardingAudience
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.OfflinePlayer
import org.bukkit.World
import org.bukkit.event.Event
import org.bukkit.event.player.PlayerJoinEvent
import org.spigotmc.event.player.PlayerSpawnLocationEvent

class SillyGameScene(val world: World, val mapData: MapData, val team: Teams.Team, id: String) : OfflinePlayerScene(id), ForwardingAudience {
    val playerSpawnPositions = mapData.positions.all("player_spawn").resolve(world)
    var avaliableSpawns = playerSpawnPositions.toMutableList()
    val initalised = mutableListOf<UUID>()

    override fun init() {
        super.init()
        team.offlinePlayers.forEach {
            addPlayer(it)

            val player = it.player ?: return@forEach
            player.teleportAsync(getPlayerSpawn())
            player.reset()
            player.gameMode = GameMode.SURVIVAL
            initalised += player.uniqueId
        }
    }

    override fun setupComponents(player: OfflinePlayer) {
        player.container.add<PlayerProtection>()
    }

    override fun setupComponents() {
        this.add(ClueManager(this, mapData))
        this.add(HopperHandler(this, mapData))
        this.add(KitHandler(this, mapData))
    }

    @Subscribe
    fun location(evt: PlayerSpawnLocationEvent) {
        if (initalised.contains(evt.player.uniqueId)) return
        evt.spawnLocation = getPlayerSpawn()
    }

    @Subscribe
    fun join(evt: PlayerJoinEvent) {
        if (initalised.contains(evt.player.uniqueId)) return
        evt.player.reset()
        evt.player.gameMode = GameMode.SURVIVAL
        initalised += evt.player.uniqueId
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

    fun addScore(score: Int) {
        team.totalScore += score
    }

    override fun audiences() = players.mapNotNull { it.player }
    override fun filter(event: Event) = event.filterWorld(world) || event.filterUUIDs(uuids, false)
}