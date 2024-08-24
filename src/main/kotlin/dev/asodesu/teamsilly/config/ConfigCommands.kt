package dev.asodesu.teamsilly.config

import cloud.commandframework.arguments.standard.EnumArgument
import cloud.commandframework.arguments.standard.StringArgument
import cloud.commandframework.kotlin.MutableCommandBuilder
import dev.asodesu.origami.utilities.error
import dev.asodesu.origami.utilities.success
import java.lang.IllegalStateException
import org.bukkit.Location
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object ConfigCommands {

    fun register(builder: MutableCommandBuilder<CommandSender>) {
        builder.registerCopy("reload") {
            argument(
                StringArgument.builder<CommandSender?>("config")
                    .asRequired()
                    .withSuggestionsProvider { _, _ -> Config.values.keys.toList() }
            )
            handler {
                val configKey = it.get<String>("config")
                val config = Config.values[configKey]
                    ?: return@handler it.sender.error("No config with key $configKey")
                config.reload()
                it.sender.success("Reloaded config!")
            }
        }

        builder.registerCopy("locations") {
            argument(
                StringArgument.builder<CommandSender?>("path")
                    .asRequired()
                    .withSuggestionsProvider { _, _ -> Locations.values.keys.toList() }
            )
            registerCopy("set") {
                senderType(Player::class.java)
                argument(EnumArgument.optional(LocationPoint::class.java, "point", LocationPoint.FEET))
                handler {
                    val path = it.get<String>("path")
                    val value = Locations.values[path]
                        ?: return@handler it.sender.error("No location with path $path")
                    val locationConfig = value as? Config.Value<Location>
                        ?: return@handler it.sender.error("Location with path '$path' is not a single location value")
                    val location = it.get<LocationPoint>("point")
                        .func(it.sender as Player)
                        ?: return@handler it.sender.error("You do not have a valid location.")
                    locationConfig.set(location)
                    it.sender.success("Set location $path to your current location!")
                }
            }

            registerCopy("add") {
                senderType(Player::class.java)
                argument(EnumArgument.optional(LocationPoint::class.java, "point", LocationPoint.FEET))
                handler {
                    val path = it.get<String>("path")
                    val value = Locations.values[path]
                        ?: return@handler it.sender.error("No location with path $path")
                    val locationConfig = value as? Config.Value<List<Location>>
                        ?: return@handler it.sender.error("Location with path '$path' is not a single location value")
                    val list = locationConfig.get().toMutableList()
                    val location = it.get<LocationPoint>("point")
                        .func(it.sender as Player)
                        ?: return@handler it.sender.error("You do not have a valid location.")
                    list.add(location)
                    locationConfig.set(list)
                    it.sender.success("Added your current location to $path!")
                }
            }

            registerCopy("tp") {
                senderType(Player::class.java)
                handler {
                    val path = it.get<String>("path")
                    val value = Locations.values[path]
                        ?: return@handler it.sender.error("No location with path $path")
                    val locationConfig = value as? Config.Value<Location>
                        ?: return@handler it.sender.error("Location with path '$path' is not a single location value")
                    (it.sender as Player).teleport(locationConfig.get())
                }
            }
        }
    }

    enum class LocationPoint(val func: (Player) -> Location?) {
        TARGET({ it.getTargetBlockExact(16)?.location }),
        FEET({ it.location })
    }

}