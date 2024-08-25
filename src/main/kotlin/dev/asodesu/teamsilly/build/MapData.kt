package dev.asodesu.teamsilly.build

import dev.asodesu.teamsilly.build.element.BoundingBoxElement
import dev.asodesu.teamsilly.build.element.PositionElement
import dev.asodesu.teamsilly.build.element.all
import java.io.File
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
class MapData {
    val boundingBoxes = mutableListOf<BoundingBoxElement>()
    val positions = mutableListOf<PositionElement>()

    fun getIdAutocompletes(type: String): List<String> {
        val set = mutableSetOf<String>()
        if (type == "bound") {
            set += boundingBoxes.map { it.id }
        } else if (type == "position") {
            set += positions.map { it.id }
        }
        return set.toList()
    }
    fun getAttribAutocompletes(type: String, id: String): List<String> {
        val set = mutableSetOf<String>()
        if (type == "bound") {
            set += boundingBoxes.all(id).flatMap { it.attributes.toStrings() }
        } else if (type == "position") {
            set += positions.all(id).flatMap { it.attributes.toStrings() }
        }
        return set.toList()
    }
    private fun Map<String, String>.toStrings(): List<String> {
        return this.map { "${it.key}=${it.value}" }
    }

    fun save(file: File) {
        file.writeText(Json.encodeToString(this))
    }

    companion object {
        fun load(file: File) = Json.decodeFromString<MapData>(file.readText())
    }
}