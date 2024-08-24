package dev.asodesu.teamsilly.build.element

import dev.asodesu.teamsilly.serializers.BoundingBoxSerializer
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.bukkit.World
import org.bukkit.util.BoundingBox

@Serializable
class BoundingBoxElement(
    override val id: String,
    @Serializable(with = BoundingBoxSerializer::class) val box: BoundingBox,

    override val attributes: Map<String, String> = mapOf()
) : MapElement<BoundingBox> {
    override fun resolve(world: World) = box
}