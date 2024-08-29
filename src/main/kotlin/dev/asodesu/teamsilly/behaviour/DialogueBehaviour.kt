package dev.asodesu.teamsilly.behaviour

import dev.asodesu.origami.engine.player.OnlinePlayerBehaviour
import dev.asodesu.origami.engine.player.PlayerBehaviour
import dev.asodesu.origami.engine.player.PlayerBehaviourContainer
import dev.asodesu.origami.engine.wiring.annotations.Subscribe
import dev.asodesu.origami.utilities.bukkit.runLater
import dev.asodesu.origami.utilities.send
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.scheduler.BukkitTask
import kotlin.time.Duration

class DialogueBehaviour(private val lines: List<Pair<String, Duration>>, c: PlayerBehaviourContainer, val finish: () -> Unit) : PlayerBehaviour(c) {
    private var index = 0
    private var task: BukkitTask? = null

    init {
        sendLine()
    }

    private fun sendLine() {
        val (line, lineDuration) = lines[index++]
        player.send(line)
        task = if (index >= lines.size) runLater(lineDuration) {
            this@DialogueBehaviour.destroy()
            finish()
        } else runLater(lineDuration) {
            sendLine()
        }
    }

    @Subscribe
    private fun disconnect(evt: PlayerQuitEvent) = this.destroy()

    override fun destroy() {
        super.destroy()
        this.task?.cancel()
    }
}