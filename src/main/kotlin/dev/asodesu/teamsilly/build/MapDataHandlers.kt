package dev.asodesu.teamsilly.build

import cloud.commandframework.arguments.flags.CommandFlag
import cloud.commandframework.arguments.standard.BooleanArgument
import cloud.commandframework.arguments.standard.StringArgument
import cloud.commandframework.kotlin.MutableCommandBuilder
import dev.asodesu.origami.engine.addBy
import dev.asodesu.origami.engine.get
import dev.asodesu.origami.engine.has
import dev.asodesu.origami.engine.player.container
import dev.asodesu.origami.engine.removeAll
import dev.asodesu.origami.engine.scopes.global
import dev.asodesu.origami.engine.scopes.scope
import dev.asodesu.origami.utilities.bukkit.dataFolder
import dev.asodesu.origami.utilities.commands.commandManager
import dev.asodesu.origami.utilities.error
import dev.asodesu.origami.utilities.miniMessage
import dev.asodesu.origami.utilities.success
import dev.asodesu.teamsilly.build.behaviours.MapEditorBehaviour
import dev.asodesu.teamsilly.build.behaviours.mapEditorAttributes
import dev.asodesu.teamsilly.build.behaviours.mapEditorId
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

object MapDataHandlers {
    val mapsFolder = dataFolder.resolve("maps")
    val maps = mutableMapOf<String, MapData>()

    init {
        if (!mapsFolder.exists()) mapsFolder.mkdir()
    }

    fun getOrLoad(name: String): MapData {
        if (maps.containsKey(name)) return maps[name]!!
        return load(name)
    }

    fun load(name: String): MapData {
        val file = mapsFolder.resolve("$name.json")
        val map = if (file.exists()) MapData.load(file)
        else MapData()
        maps[name] = map
        return map
    }

    fun save(name: String, data: MapData) {
        val file = mapsFolder.resolve("$name.json")
        data.save(file)
    }

    fun register(builder: MutableCommandBuilder<CommandSender>) {
        builder.registerCopy("edit") {
            senderType(Player::class)
            mapArgument()
            argument(BooleanArgument.optional("reload", false))
            handler {
                val player = it.sender as Player
                val behaviours = player.container
                if (behaviours.has<MapEditorBehaviour>())
                    return@handler it.sender.error("You are already editing a map!")

                val mapKey = it.get<String>("map")
                val reload = it.get<Boolean>("reload")
                val map = if (reload) load(mapKey) else getOrLoad(mapKey)
                scope(global) {
                    behaviours.addBy { p -> MapEditorBehaviour(map, mapKey, p) }
                }
            }
        }

        builder.registerCopy("close") {
            senderType(Player::class)
            handler {
                val player = it.sender as Player
                val behaviours = player.container
                if (!behaviours.has<MapEditorBehaviour>())
                    return@handler it.sender.error("You are not editing a map!")
                behaviours.removeAll<MapEditorBehaviour>()
                it.sender.success("Closed map <dark_green><b>(Any unsaved changes will not auto-save)")
            }
        }

        builder.registerCopy("save") {
            senderType(Player::class)
            handler {
                val player = it.sender as Player
                val behaviours = player.container
                if (!behaviours.has<MapEditorBehaviour>())
                    return@handler it.sender.error("You are not editing a map!")
                val mapEditor = behaviours.get<MapEditorBehaviour>()
                mapEditor.save()
                it.sender.success("Saved Changes!")
            }
        }

        builder.registerCopy("undo") {
            senderType(Player::class)
            handler {
                val player = it.sender as Player
                val behaviours = player.container
                if (!behaviours.has<MapEditorBehaviour>())
                    return@handler it.sender.error("You are not editing a map!")
                val mapEditor = behaviours.get<MapEditorBehaviour>()
                mapEditor.undo()
            }
        }

        builder.registerCopy("position_item") {
            senderType(Player::class)
            argument(StringArgument.single("id"))
            mutate {
                it.flag(
                    CommandFlag.builder("attr")
                        .withArgument(StringArgument.single<CommandSender>("value"))
                        .asRepeatable()
                )
            }
            handler {
                val id = it.get<String>("id")
                val attributes = it.flags()
                    .getAll<String>("attr")
                    .associate { attr ->
                        val split = attr.split("=")
                        if (split.size < 2) return@handler it.sender.error("Invalid attribute flag '$attr'")
                        return@associate split[0] to split[1]
                    }

                val item = ItemStack(Material.ARMOR_STAND)
                item.mapEditorId = id
                if (attributes.isNotEmpty()) item.mapEditorAttributes = attributes
                item.editMeta { meta ->
                    meta.itemName(miniMessage("<origami>$id</origami> <gray>(${attributes.size} attrs)"))
                    meta.setEnchantmentGlintOverride(true)
                }

                val playerInv = (it.sender as Player).inventory
                playerInv.addItem(item)
            }
        }
    }

    private fun MutableCommandBuilder<CommandSender>.mapArgument() {
        argument(
            StringArgument.builder<CommandSender?>("map")
                .greedy()
                .withSuggestionsProvider { _, _ ->
                    maps.keys.toList() + (mapsFolder.listFiles()?.map { it.nameWithoutExtension } ?: emptyList())
                }
        )
    }

}