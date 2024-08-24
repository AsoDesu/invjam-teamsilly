package dev.asodesu.teamsilly.build.behaviours

import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.ListPersistentDataType
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType


val KEY_MAPEDITOR_ATTRIBUTES = NamespacedKey("asodesu", "mapeditor_attributes")
var ItemStack.mapEditorAttributes: Map<String, String>
    get() {
        val meta = this.itemMeta ?: return mapOf()
        val container = meta.persistentDataContainer.get(KEY_MAPEDITOR_ATTRIBUTES, PersistentDataType.TAG_CONTAINER) ?: return mapOf()
        return container.keys.associate {
            it.key to container.get(it, PersistentDataType.STRING)!!
        }
    }
    set(value) {
        val meta = this.itemMeta ?: return
        val container = meta.persistentDataContainer
        val attributes = container.adapterContext.newPersistentDataContainer()
        value.forEach { (key, value) ->
            attributes.set(NamespacedKey("c", key), PersistentDataType.STRING, value)
        }
        container.set(KEY_MAPEDITOR_ATTRIBUTES, PersistentDataType.TAG_CONTAINER, attributes)
        this.itemMeta = meta
    }

val KEY_MAPEDITOR_ID = NamespacedKey("asodesu", "mapeditor_id")
var ItemStack.mapEditorId: String?
    get() = this.itemMeta?.persistentDataContainer?.get(KEY_MAPEDITOR_ID, PersistentDataType.STRING)
    set(value) {
        this.editMeta {
            if (value == null) it.persistentDataContainer.remove(KEY_MAPEDITOR_ID)
            else it.persistentDataContainer.set(KEY_MAPEDITOR_ID, PersistentDataType.STRING, value)
        }
    }