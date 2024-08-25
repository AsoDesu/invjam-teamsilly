package dev.asodesu.teamsilly.build.element

import org.bukkit.World
import org.bukkit.util.Vector

interface MapElement<T> {
    val id: String
    val attributes: Map<String, String>
    fun resolve(world: World): T

    fun getVector(key: String): Vector? {
        val components = attributes[key]?.split(",") ?: return null
        if (components.size != 3) return null
        return Vector(components[0].toDouble(), components[1].toDouble(), components[2].toDouble())
    }
}