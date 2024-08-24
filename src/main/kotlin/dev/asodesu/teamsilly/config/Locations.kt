package dev.asodesu.teamsilly.config

import org.bukkit.Location

object Locations : Config("locations") {
    var playerSpawns by value<List<Location>>("playerSpawns", listOf())
    var clueButtons by value<List<Location>>("clueButtons", listOf())

    // parkourclue
    val sugarStartingLocation by value<Location>("testClue1.starting")
}