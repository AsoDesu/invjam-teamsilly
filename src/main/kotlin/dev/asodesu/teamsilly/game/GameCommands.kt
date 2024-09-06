package dev.asodesu.teamsilly.game

import cloud.commandframework.arguments.standard.IntegerArgument
import cloud.commandframework.arguments.standard.StringArgument
import cloud.commandframework.bukkit.parsers.PlayerArgument
import cloud.commandframework.kotlin.MutableCommandBuilder
import dev.asodesu.origami.engine.get
import dev.asodesu.origami.engine.scene.Scenes
import dev.asodesu.origami.utilities.bukkit.allPlayers
import dev.asodesu.origami.utilities.error
import dev.asodesu.teamsilly.build.MapDataHandlers
import dev.asodesu.teamsilly.clues.ClueManager
import dev.asodesu.teamsilly.clues.display.ActiveClueDisplay
import dev.asodesu.teamsilly.utils.team
import org.bukkit.GameMode
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object GameCommands {

    fun register(builder: MutableCommandBuilder<CommandSender>) {
        builder.registerCopy("worlds") {
            registerCopy("create") {
                senderType(Player::class.java)
                handler {
                    WorldManager.create((it.sender as Player).world)
                }
            }

            registerCopy("load") {
                handler {
                    WorldManager.load()
                }
            }

            registerCopy("delete") {
                handler {
                    WorldManager.delete()
                }
            }
        }

        builder.registerCopy("start") {
            handler {
                val mapData = MapDataHandlers.getOrLoad("city")
                val worlds = WorldManager.worlds.toMutableList()
                Teams.all.forEachIndexed { i, team ->
                    team.totalScore = 0
                    team.players.forEach { it.score = 0 }

                    val world = worlds.removeFirst()
                    Scenes.register(SillyGameScene(world, mapData, team,"sillygame/$i"))
                }

                allPlayers.forEach {
                    if (it.team == null) {
                        it.gameMode = GameMode.SPECTATOR
                        it.teleport(worlds.first().spawnLocation)
                    }
                }
            }
        }

        builder.registerCopy("teams") {
            registerCopy("update") {
                handler {
                    Teams.updateTeams()
                }
            }

            registerCopy("add") {
                argument(PlayerArgument.of("player"))
                argument(StringArgument.builder<CommandSender?>("team").greedy().withSuggestionsProvider { _, _ -> Teams.all.map { it.name } })
                handler {
                    val name = it.get<String>("team")
                    val player = it.get<Player>("player")

                    val team = Teams.all.find { team -> team.name == name } ?: return@handler
                    val index = Teams.all.indexOf(team)
                    // holy shit
                    val players = team.players.toMutableList()
                    players.add(Teams.TeamPlayer(player.uniqueId.toString(), 0))

                    val newTeam = Teams.Team(team.name, team.totalScore, players)
                    Teams.all[index] = newTeam

                    Teams.updatePlayers()
                }
            }
        }

        builder.registerCopy("end") {
            handler {
                Scenes.map.toMap().forEach {
                    val silly = it.value as? SillyGameScene ?: return@forEach
                    Scenes.unregister(silly)
                }

                Teams.commitPoints()
            }
        }

        builder.registerCopy("debug") {
            registerCopy("clue_slot") {
                argument(IntegerArgument.of("slot"))
                registerCopy("recreate") {
                    handler {
                        val slotIndex = it.get<Int>("slot")
                        val scene = Scenes.map.entries.first().value as SillyGameScene
                        val slot = scene.get<ClueManager>().clueSlots.getOrNull(slotIndex)
                            ?: return@handler it.sender.error("Invalid slot.")

                        slot.display?.destroy()
                        slot.display = null

                        if (slot.clue != null) slot.display = ActiveClueDisplay(slot.displayOrigin, slot.clue!!)
                    }
                }

                registerCopy("get") {
                    senderType(Player::class.java)
                    handler {
                        val slotIndex = it.get<Int>("slot")
                        val scene = Scenes.map.entries.first().value as SillyGameScene
                        val slot = scene.get<ClueManager>().clueSlots.getOrNull(slotIndex)
                            ?: return@handler it.sender.error("Invalid slot.")

                        (it.sender as Player).inventory.addItem(slot.clue?.item!!)
                    }
                }
            }
        }
    }

}