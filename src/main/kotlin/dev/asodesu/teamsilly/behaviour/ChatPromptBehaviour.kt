package dev.asodesu.teamsilly.behaviour

import dev.asodesu.origami.engine.player.PlayerBehaviour
import dev.asodesu.origami.engine.player.PlayerBehaviourContainer
import dev.asodesu.origami.engine.wiring.annotations.Subscribe
import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerQuitEvent

class ChatPromptBehaviour(c: PlayerBehaviourContainer, val onInput: (Player, String, ChatPromptBehaviour) -> Unit) : PlayerBehaviour(c) {

    @Subscribe
    fun chat(evt: AsyncChatEvent) {
        val text = PlainTextComponentSerializer.plainText().serialize(evt.message())
        evt.isCancelled = true
        onInput(evt.player, text, this)
    }

    @Subscribe
    private fun disconnect(evt: PlayerQuitEvent) = this.destroy()
}