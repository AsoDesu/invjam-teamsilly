package dev.asodesu.teamsilly.build.element

import java.lang.IllegalStateException
import org.bukkit.Location
import org.bukkit.World

fun <T : MapElement<*>> List<T>.all(id: String): List<T> {
    return this.filter { if (id == "*") true else it.id == id }
}

fun <T : MapElement<*>> List<T>.single(id: String): T {
    val all = this.all(id)
    if (all.isEmpty())
        throw IllegalStateException("No Map Element with id '$id'")
    if (all.size > 1)
        throw IllegalStateException("There are multiple map element with id '$id'")
    return all.first()
}

fun <T : MapElement<*>> List<T>.withAttribute(key: String, value: Any): List<T> {
    return this.filter { it.attributes[key] == value.toString() }
}
fun <T : MapElement<*>> List<T>.withAttributes(attributes: Map<String, String>): List<T> {
    return this.filter { element -> attributes.all { element.attributes[it.key] == it.value } }
}
fun <T : MapElement<*>> List<T>.withAttributes(vararg attributes: Pair<String, String>): List<T> {
    return this.filter { element -> attributes.all { element.attributes[it.first] == it.second } }
}

fun <K, T : MapElement<K>> List<T>.resolve(world: World): List<K> {
    return this.map { it.resolve(world) }
}

fun <K, T : MapElement<K>> List<T>.resolveSingle(world: World): K {
    return this.single().resolve(world)
}

fun Location.noRotation() = Location(this.world, this.x, this.y, this.z)