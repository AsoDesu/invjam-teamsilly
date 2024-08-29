package dev.asodesu.teamsilly.utils

import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.entity.BlockDisplay
import org.bukkit.util.BoundingBox
import org.bukkit.util.Transformation
import org.joml.Quaternionf
import org.joml.Vector3f
import kotlin.concurrent.thread

fun BoundingBox.glowingDisplay(world: World, color: Color): BlockDisplay {
    return world.spawn(this.min.toLocation(world), BlockDisplay::class.java) {
        it.block = Material.GREEN_STAINED_GLASS.createBlockData()
        it.isGlowing = true
        it.transformation = Transformation(
            Vector3f(),
            Quaternionf(),
            Vector3f(widthX.toFloat(), height.toFloat(), widthZ.toFloat()),
            Quaternionf()
        )
        it.glowColorOverride = color
    }
}

fun BoundingBox.playersInBox(world: World) = world.players.filter { this.contains(it.boundingBox) }
fun Iterable<BoundingBox>.playersInBox(world: World) = world.players.filter { this.any { box -> box.contains(it.boundingBox) } }
fun Iterable<BoundingBox>.contains(loc: Location) = this.any { it.contains(loc.toVector()) }