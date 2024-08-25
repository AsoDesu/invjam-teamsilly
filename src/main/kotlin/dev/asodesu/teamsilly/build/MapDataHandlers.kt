package dev.asodesu.teamsilly.build

import cloud.commandframework.arguments.flags.CommandFlag
import cloud.commandframework.arguments.standard.BooleanArgument
import cloud.commandframework.arguments.standard.StringArgument
import cloud.commandframework.context.CommandContext
import cloud.commandframework.kotlin.MutableCommandBuilder
import dev.asodesu.origami.engine.addBy
import dev.asodesu.origami.engine.get
import dev.asodesu.origami.engine.has
import dev.asodesu.origami.engine.player.container
import dev.asodesu.origami.engine.removeAll
import dev.asodesu.origami.engine.scopes.global
import dev.asodesu.origami.engine.scopes.scope
import dev.asodesu.origami.utilities.bukkit.dataFolder
import dev.asodesu.origami.utilities.error
import dev.asodesu.origami.utilities.miniMessage
import dev.asodesu.origami.utilities.success
import dev.asodesu.teamsilly.build.behaviours.MapEditorBehaviour
import dev.asodesu.teamsilly.build.behaviours.mapEditorAttributes
import dev.asodesu.teamsilly.build.behaviours.mapEditorId
import dev.asodesu.teamsilly.build.element.all
import dev.asodesu.teamsilly.build.element.single
import dev.asodesu.teamsilly.build.element.withAttributes
import dev.asodesu.teamsilly.utils.getSelection
import dev.asodesu.teamsilly.utils.setSelection
import java.lang.Exception
import org.bukkit.Color
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
        builder.senderType(Player::class)
        builder.registerCopy("open") {
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
            handler {
                val mapEditor = it.getMapEditor() ?: return@handler
                mapEditor.save()
                it.sender.success("Saved Changes!")
            }
        }

        builder.registerCopy("undo") {
            handler {
                val mapEditor = it.getMapEditor() ?: return@handler
                mapEditor.undo()
            }
        }

        builder.registerCopy("edit") {
            registerCopy("new") { createCommands() }
            registerCopy("select") { selectCommands() }
            registerCopy("delete") { deleteCommands() }
        }
    }

    private fun MutableCommandBuilder<CommandSender>.createCommands() {
        registerCopy("position") {
            idAttribArguments()
            handler {
                val id = it.get<String>("id")
                val attributes = it.getAttributes() ?: return@handler

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

        registerCopy("bound") {
            idAttribArguments()
            handler {
                val player = it.sender as Player
                val id = it.get<String>("id")
                val attributes = it.getAttributes() ?: return@handler
                val mapEditor = it.getMapEditor() ?: return@handler

                mapEditor.addBoundingBox(player.getSelection(), id, attributes)
            }
        }
    }
    private fun MutableCommandBuilder<CommandSender>.selectCommands() {
        registerCopy("bound") {
            idAttribAutocompleteArguments("bound")
            handler {
                val id = it.get<String>("id")
                val attributes = it.getAttributes() ?: return@handler
                val mapEditor = it.getMapEditor() ?: return@handler

                mapEditor.map.boundingBoxes
                    .withAttributes(attributes)
                    .all(id)
                    .forEach { box ->
                        mapEditor.highlightBound(Color.AQUA, box.box)
                    }
            }
        }
        registerCopy("position") {
            idAttribAutocompleteArguments("position")
            handler {
                val player = it.sender as Player
                val id = it.get<String>("id")
                val attributes = it.getAttributes() ?: return@handler
                val mapEditor = it.getMapEditor() ?: return@handler

                var count = 0
                val positions = mapEditor.map.positions
                positions.all(id)
                    .withAttributes(attributes)
                    .forEach { pos ->
                    mapEditor.highlightPosition("<aqua>⏺</aqua>", pos.resolve(player.world), pos.id)
                    count++
                }
                it.sender.success("Highlighted $count positions.")
            }
        }
    }
    private fun MutableCommandBuilder<CommandSender>.deleteCommands() {
        registerCopy("bound") {
            idAttribAutocompleteArguments("bound")
            handler {
                val id = it.get<String>("id")
                val attributes = it.getAttributes() ?: return@handler
                val mapEditor = it.getMapEditor() ?: return@handler

                var count = 0
                val boxes = mapEditor.map.boundingBoxes
                boxes.all(id).withAttributes(attributes).forEach { box ->
                    mapEditor.highlightBound(Color.RED, box.box)
                    boxes.remove(box)
                    count++
                }
                it.sender.success("Removed $count bounding boxes.")
            }
        }
        registerCopy("position") {
            idAttribAutocompleteArguments("position")
            handler {
                val player = it.sender as Player
                val id = it.get<String>("id")
                val attributes = it.getAttributes() ?: return@handler
                val mapEditor = it.getMapEditor() ?: return@handler

                var count = 0
                val positions = mapEditor.map.positions
                positions.all(id).withAttributes(attributes).forEach { pos ->
                    mapEditor.highlightPosition("<red>❌</red>", pos.resolve(player.world), pos.id)
                    positions.remove(pos)
                    count++
                }
                it.sender.success("Removed $count positions.")
            }
        }
    }

    private fun CommandContext<CommandSender>.getAttributes(): Map<String, String>? {
        return this.flags()
            .getAll<String>("attr")
            .associate { attr ->
                val split = attr.split("=")
                if (split.size < 2) {
                    sender.error("Invalid attribute flag '$attr'")
                    return null
                }
                return@associate split[0] to split[1]
            }
    }
    private fun CommandContext<CommandSender>.getMapEditor(): MapEditorBehaviour? {
        val player = sender as Player
        val behaviours = player.container
        if (!behaviours.has<MapEditorBehaviour>()) {
            sender.error("You are not editing a map!")
            return null
        }
        return behaviours.get<MapEditorBehaviour>()
    }
    private fun MutableCommandBuilder<CommandSender>.idAttribArguments() {
        argument(StringArgument.single("id"))
        mutate {
            it.flag(
                CommandFlag.builder("attr")
                    .withArgument(StringArgument.single<CommandSender>("value"))
                    .asRepeatable()
            )
        }
    }
    private fun MutableCommandBuilder<CommandSender>.idAttribAutocompleteArguments(type: String) {
        argument(
            StringArgument.builder<CommandSender>("id")
                .single()
                .withSuggestionsProvider { ctx, _ ->
                    ctx.getMapEditor()?.map?.getIdAutocompletes(type) ?: emptyList()
                }
        )
        mutate {
            it.flag(
                CommandFlag.builder("attr")
                    .withArgument(
                        StringArgument.builder<CommandSender>("value")
                            .single()
                            .withSuggestionsProvider { ctx, _ ->
                                val id = ctx.getOrDefault<String>("id", "*")
                                ctx.getMapEditor()?.map?.getAttribAutocompletes(type, id) ?: emptyList()
                            }
                    )
                    .asRepeatable()
            )
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