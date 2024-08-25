package dev.asodesu.teamsilly.utils

import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.entity.BlockDisplay
import org.bukkit.util.BoundingBox
import org.bukkit.util.Transformation
import org.joml.Quaternionf
import org.joml.Vector3f

fun BoundingBox.glowingDisplay(world: World, color: Color): BlockDisplay {
    return world.spawn(this.min.toLocation(world), BlockDisplay::class.java) {
        it.block = Material.GREEN_STAINED_GLASS.createBlockData()
        it.isGlowing = true
        it.transformation = Transformation(
            Vector3f(),
            Quaternionf(),
            Vector3f(widthX.toFloat() + 1f, height.toFloat() + 1f, widthZ.toFloat() + 1f),
            Quaternionf()
        )
        it.glowColorOverride = color
    }
}