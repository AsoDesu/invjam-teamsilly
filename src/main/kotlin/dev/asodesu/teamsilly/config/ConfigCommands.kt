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
    }

}