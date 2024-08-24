package dev.asodesu.teamsilly.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.bukkit.util.BoundingBox
import org.bukkit.util.Vector

object VectorSerializer : KSerializer<Vector> {
    override val descriptor = VectorSurrogate.serializer().descriptor

    override fun deserialize(decoder: Decoder): Vector {
        val surrogate = decoder.decodeSerializableValue(VectorSurrogate.serializer())
        return Vector(surrogate.x, surrogate.y, surrogate.z)
    }

    override fun serialize(encoder: Encoder, value: Vector) {
        val surrogate =
            VectorSurrogate(value.x, value.y, value.z)
        encoder.encodeSerializableValue(VectorSurrogate.serializer(), surrogate)
    }

    @Serializable
    private class VectorSurrogate(val x: Double, val y: Double, val z: Double)
}