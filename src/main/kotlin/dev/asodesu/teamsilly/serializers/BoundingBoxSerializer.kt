package dev.asodesu.teamsilly.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.bukkit.util.BoundingBox

object BoundingBoxSerializer : KSerializer<BoundingBox> {
    override val descriptor = BoundingBoxSurrogate.serializer().descriptor

    override fun deserialize(decoder: Decoder): BoundingBox {
        val surrogate = decoder.decodeSerializableValue(BoundingBoxSurrogate.serializer())
        return BoundingBox(
            surrogate.minX, surrogate.minY, surrogate.minZ,
            surrogate.maxX, surrogate.maxY, surrogate.maxZ,
        )
    }

    override fun serialize(encoder: Encoder, value: BoundingBox) {
        val surrogate =
            BoundingBoxSurrogate(value.minX, value.minY, value.minZ, value.maxX, value.maxY, value.maxZ)
        encoder.encodeSerializableValue(BoundingBoxSurrogate.serializer(), surrogate)
    }

    @Serializable
    private class BoundingBoxSurrogate(
        val minX: Double,
        val minY: Double,
        val minZ: Double,
        val maxX: Double,
        val maxY: Double,
        val maxZ: Double,
    )
}