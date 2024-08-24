package dev.asodesu.teamsilly.build.element

import dev.asodesu.teamsilly.serializers.VectorSerializer
import io.papermc.paper.math.FinePosition
import kotlinx.serialization.Serializable
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.util.Vector

@Serializable
class PositionElement(
    override val id: String,
    @Serializable(with = VectorSerializer::class) val position: Vector,
    val yaw: Float = 0f,
    val pitch: Float = 0f,

    override val attributes: Map<String, String> = mapOf()
) : MapElement<Location> {
    constructor(id: String, loc: Location, attributes: Map<String, String>) : this(
        id,
        loc.toVector(),
        loc.yaw, loc.pitch,
        attributes
    )

    override fun resolve(world: World) = position.toLocation(world, yaw, pitch)
}