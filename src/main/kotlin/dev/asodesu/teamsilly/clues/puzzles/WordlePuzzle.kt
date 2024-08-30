package dev.asodesu.teamsilly.clues.puzzles

import dev.asodesu.origami.engine.consumeCooldown
import dev.asodesu.origami.engine.player.container
import dev.asodesu.origami.engine.replace
import dev.asodesu.origami.engine.wiring.annotations.Subscribe
import dev.asodesu.origami.utilities.send
import dev.asodesu.origami.utilities.ticks
import dev.asodesu.origami.utilities.warning
import dev.asodesu.teamsilly.behaviour.ChatPromptBehaviour
import dev.asodesu.teamsilly.build.MapData
import dev.asodesu.teamsilly.build.element.all
import dev.asodesu.teamsilly.build.element.noRotation
import dev.asodesu.teamsilly.build.element.resolve
import dev.asodesu.teamsilly.build.element.resolveSingle
import dev.asodesu.teamsilly.clues.CluePuzzle
import org.bukkit.World
import org.bukkit.event.player.PlayerInteractEvent

class WordlePuzzle(id: String, val world: World, mapData: MapData) : CluePuzzle(id, mapData, world) {
    override val name: String = "Word Puzzle"
    val triggerBlock = mapData.positions.all("worlde_trigger").withPuzzleId().resolveSingle(world).noRotation()

    val link = "https://mywordle.strivemath.com/?word=yvrrd"
    val answer = "chaos"

    @Subscribe
    fun interact(evt: PlayerInteractEvent) {
        val block = evt.clickedBlock ?: return
        if (block.location.toBlockLocation() != triggerBlock.toBlockLocation()) return
        if (evt.player.consumeCooldown("wordle/click", 1.ticks)) return

        evt.player.send("<obf><red>[</red><aqua>]</aqua></obf> The key to the safe seems to be locked behind.... a wordle??")
        evt.player.send("<obf><red>[</red><aqua>]</aqua></obf> <u><click:open_url:'$link'><hover:show_text:'Opens <origami>$link'>Click to open the wordle</hover></click>")
        evt.player.send("<obf><red>[</red><aqua>]</aqua></obf> <gray>Type answer in the chat, or type 'exit' to cancel.")

        val container = evt.player.container
        container.replace(scope = this.scope, instance = ChatPromptBehaviour(container) { player, input, behaviour ->
            if (input.equals("exit", ignoreCase = true) || isCompleted) {
                if (isCompleted) player.warning("This puzzle has already been completed!")
                else player.warning("Left current question. You can start again by clicking on the monitor")
                return@ChatPromptBehaviour behaviour.destroy()
            }

            player.send("\n<green>[ANSWER] ${player.name}:</green> $input")
            if (input.equals(answer, ignoreCase = true)) {
                this.complete(player)
                behaviour.destroy()
            } else {
                evt.player.send("<obf><red>[</red><aqua>]</aqua></obf> That's not the correct answer :(")
            }
        })
    }
}