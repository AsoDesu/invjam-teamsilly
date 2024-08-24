package dev.asodesu.teamsilly.config

import dev.asodesu.origami.utilities.bukkit.dataFolder
import org.bukkit.configuration.file.YamlConfiguration
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

abstract class Config(name: String) : YmlConfig(name) {
    companion object {
        val values = mutableMapOf<String, Config>(
            "locations" to Locations
        )

        fun init() {
            values.forEach { it.value.reload() }
        }
    }
    val values = mutableMapOf<String, Value<*>>()

    override fun reload() {
        super.reload()
        values.forEach { it.value.load() }
    }

    fun <T> value(path: String, default: T? = null): Value<T> {
        val value = Value(path, this, default)
        values[path] = value
        return value
    }

    class Value<T>(val path: String, val config: Config, val default: T? = null) : ReadWriteProperty<Any, T> {
        var value: T? = default

        fun load() {
            value = config.yml.get(path, default) as? T
        }

        override fun getValue(thisRef: Any, property: KProperty<*>) = get()
        override fun setValue(thisRef: Any, property: KProperty<*>, value: T) = set(value)

        fun get() = value
            ?: throw IllegalStateException("Config Value '$path' has not been initialised")

        fun set(value: T) {
            this.value = value
            config.yml.set(path, value)
            config.save()
        }

    }

}