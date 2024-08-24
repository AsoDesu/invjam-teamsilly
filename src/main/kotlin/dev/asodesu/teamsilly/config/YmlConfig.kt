package dev.asodesu.teamsilly.config

import dev.asodesu.origami.utilities.bukkit.dataFolder
import org.bukkit.configuration.file.YamlConfiguration
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

abstract class YmlConfig(val name: String) {
    private val file = dataFolder.resolve("$name.yml")
    val yml = YamlConfiguration()

    open fun reload() {
        if (!file.exists()) return
        yml.load(file)
    }

    open fun save() {
        yml.save(file)
    }

}