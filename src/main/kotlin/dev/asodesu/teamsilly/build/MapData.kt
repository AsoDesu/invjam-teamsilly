package dev.asodesu.teamsilly.build

import dev.asodesu.teamsilly.build.element.BoundingBoxElement
import dev.asodesu.teamsilly.build.element.MapElement
import dev.asodesu.teamsilly.build.element.PositionElement
import java.io.File
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
class MapData {
    val boundingBoxes = mutableListOf<BoundingBoxElement>()
    val positions = mutableListOf<PositionElement>()

    fun save(file: File) {
        file.writeText(Json.encodeToString(this))
    }

    companion object {
        fun load(file: File) = Json.decodeFromString<MapData>(file.readText())
    }
}