package dev.asodesu.teamsilly.build.element

import org.bukkit.World

interface MapElement<T> {
    val id: String
    val attributes: Map<String, String>
    fun resolve(world: World): T
}