package dev.asodesu.teamsilly.game

import dev.asodesu.origami.utilities.bukkit.debug
import dev.asodesu.origami.utilities.bukkit.runMain
import dev.asodesu.origami.utilities.randomString
import java.lang.Exception
import java.lang.IllegalStateException
import java.util.concurrent.CompletableFuture
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.WorldCreator

object WorldManager {
    val worlds = mutableListOf<World>()

    fun create(world: World) {
        delete()

        val futures = List(Teams.all.size) { i ->
            CompletableFuture.runAsync {
                try {
                    debug("Copying folder for world \"sillygame_$i\"...")
                    val newFolder = world.worldFolder.parentFile.resolve("sillygame_$i")
                    world.worldFolder.copyRecursively(newFolder) { file, error ->
                        debug("Failed to copy $file due to ${error.message}")
                        error.printStackTrace()
                        OnErrorAction.SKIP
                    }
                    debug("Copied folder!")
                } catch (e: Exception) {
                    debug("Failed to copy folder: ${e.message}")
                    e.printStackTrace()
                }
            }
        }
        CompletableFuture.allOf(*futures.toTypedArray()).thenAccept {
            runMain {
                debug("Finished copying folders... loading worlds")
                load()
            }
        }
    }

    fun load() {
        List(Teams.all.size) { i ->
            try {
                debug("Loading world \"sillygame_$i\"...")
                val world = WorldCreator("sillygame_$i")
                    .createWorld() ?: throw IllegalStateException("Created world was null :(")
                worlds += world
            } catch (e: Exception) {
                debug("Failed to create world: ${e.message}")
            }
        }
        debug("Created all worlds!")
    }

    fun delete() {
        worlds.forEach {
            debug("Deleting world \"${it.name}\"")
            Bukkit.unloadWorld(it, false)
            it.worldFolder.deleteRecursively()
        }

        worlds.clear()
        debug("Deleted past worlds")
    }

}