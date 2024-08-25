package dev.asodesu.teamsilly.utils

import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldedit.regions.CuboidRegion
import com.sk89q.worldedit.regions.selector.CuboidRegionSelector
import org.bukkit.entity.Player
import org.bukkit.util.BoundingBox
import org.bukkit.util.Vector

fun Player.getSelection(): BoundingBox {
    val actor = BukkitAdapter.adapt(this)
    val session = WorldEdit.getInstance().sessionManager.get(actor)
    val region = session.selection
    return BoundingBox.of(region.minimumPoint.toVector(), region.maximumPoint.toVector())
}

fun Player.setSelection(box: BoundingBox) {
    val actor = BukkitAdapter.adapt(this)
    val session = WorldEdit.getInstance().sessionManager.get(actor)
    val world = BukkitAdapter.adapt(world)

    val region = CuboidRegionSelector(world, box.min.toBlockVector3(), box.max.toBlockVector3())
    session.setRegionSelector(world, region)
}

fun BlockVector3.toVector() = Vector(x, y, z)
fun Vector.toBlockVector3() = BlockVector3.at(x, y, z)