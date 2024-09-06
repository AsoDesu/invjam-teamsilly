package dev.asodesu.teamsilly.clues.puzzles

import dev.asodesu.origami.engine.add
import dev.asodesu.origami.engine.addBy
import dev.asodesu.origami.engine.checkCooldown
import dev.asodesu.origami.engine.consumeCooldown
import dev.asodesu.origami.engine.player.PlayerBehaviourContainer
import dev.asodesu.origami.engine.player.container
import dev.asodesu.origami.engine.replace
import dev.asodesu.origami.engine.wiring.annotations.Subscribe
import dev.asodesu.origami.utilities.error
import dev.asodesu.origami.utilities.send
import dev.asodesu.origami.utilities.warning
import dev.asodesu.teamsilly.behaviour.ChatPromptBehaviour
import dev.asodesu.teamsilly.behaviour.DialogueBehaviour
import dev.asodesu.teamsilly.build.MapData
import dev.asodesu.teamsilly.build.element.all
import dev.asodesu.teamsilly.clues.CluePuzzle
import dev.asodesu.teamsilly.utils.isSpectating
import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.NamespacedKey
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.persistence.PersistentDataType
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds

class TriviaPuzzle(id: String, val world: World, mapData: MapData) : CluePuzzle(id, mapData, world) {
    override val name: String = "Trivia"
    val triggerRegion = mapData.boundingBoxes.all("trivia_npc_trigger").withPuzzleId().single().box

    val npcIdKey = NamespacedKey("asodesu", "npc_id")
    val npcId = "flower_guy"

    val lines1 = listOf(
        "<gold>[NPC] Flower Guy</gold>: Howdy there! Oh, you're lookin' for some evidence?" to 3.seconds,
        "<gold>[NPC] Flower Guy</gold>: Hmmm i might remember somethin' about that" to 3.seconds,
        "<gold>[NPC] Flower Guy</gold>: But how 'bout you indulge me first." to 3.seconds,

        "\n<gold>[NPC] Flower Guy</gold>: When burned at the stake, what dye do sea pickles make? <gray>(Type answer in chat)" to 0.seconds
    )
    val acceptableAnswersLime = listOf("lime", "lime dye")

    val lines2 = listOf(
        "<gold>[NPC] Flower Guy</gold>: Haha! Brilliant" to 3.seconds,
        "<gold>[NPC] Flower Guy</gold>: Now, One more question..." to 3.seconds,

        "\n<gold>[NPC] Flower Guy</gold>: 'I make things delicous to eat, I'm found along rivers swamps and the beach..." to 3.seconds,
        "<gold>[NPC] Flower Guy</gold>: I can grow as tall as 2 or 3, What am i?' <gray>(Type answer in chat)" to 0.seconds,
    )
    val acceptableAnswersSugar = listOf("sugar cane")

    val finalLines = listOf(
        "<gold>[NPC] Flower Guy</gold>: Amazing, well i'm true to my word," to 3.seconds,
        "<gold>[NPC] Flower Guy</gold>: Here, have this, i heard it unlocks a safe nearby!" to 3.seconds,
        "<gold>[NPC] Flower Guy</gold>: Maybe you'll find somethin' in there!" to 3.seconds
    )

    var currentQuestion = 0

    @Subscribe
    fun boundCheck(evt: PlayerMoveEvent) {
        if (evt.player.isSpectating) return
        if (evt.player.checkCooldown("trivia/npc_trigger", 10.hours)) return
        if (triggerRegion.contains(evt.to.toVector())) {
            evt.player.send("<gold>[NPC] Flower Guy</gold>: Howdy! Over here!")
            evt.player.consumeCooldown("trivia/npc_trigger", 10.hours)
        }
    }

    @Subscribe
    fun interact(evt: PlayerInteractAtEntityEvent) {
        val targetNpc = evt.rightClicked.persistentDataContainer.get(npcIdKey, PersistentDataType.STRING)
        if (targetNpc != npcId) return

        evt.isCancelled = true
        val container = evt.player.container
        if (container.has(DialogueBehaviour::class)) return
        when (currentQuestion) {
            0 -> startFirstQuestion(evt.player)
            1 -> startSecondQuestion(evt.player)
        }
    }

    private fun startFirstQuestion(player: Player) {
        currentQuestion = 0
        val container = player.container
        container.add(scope = this.scope, instance = DialogueBehaviour(lines1, container) {
            chatPrompt(container, acceptableAnswersLime) { startSecondQuestion(it) }
        })
    }

    private fun startSecondQuestion(player: Player) {
        currentQuestion = 1
        val container = player.container
        container.add(scope = this.scope, instance = DialogueBehaviour(lines2, container) {
            chatPrompt(container, acceptableAnswersSugar) { startFinish(it) }
        })
    }

    private fun startFinish(player: Player) {
        if (currentQuestion == 2) return
        currentQuestion = 2
        val container = player.container
        container.add(instance = DialogueBehaviour(finalLines, container) {
            this.complete(player)
        }, scope = this.scope)
    }

    private fun chatPrompt(container: PlayerBehaviourContainer, answers: List<String>, correct: (Player) -> Unit) {
        container.replace(scope = this.scope, instance = ChatPromptBehaviour(container) { player, input, behaviour ->
            if (input.equals("exit", ignoreCase = true) || isCompleted) {
                if (isCompleted) player.warning("This puzzle has already been completed!")
                else player.warning("Left current question. You can start again by clicking on the NPC")
                return@ChatPromptBehaviour behaviour.destroy()
            }

            player.send("<green>[ANSWER] ${player.name}:</green> $input")
            val wasCorrect = checkAcceptableAnswer(input, answers)
            if (!wasCorrect) {
                player.send("<gold>[NPC] Flower Guy</gold>: I don't think that's the right answer.. maybe try again? <gray>(Type answer in chat, Type 'exit' to leave)")
            } else {
                behaviour.destroy()
                correct(player)
            }
        })
    }

    private fun checkAcceptableAnswer(input: String, correctAnswers: List<String>): Boolean {
        val formattedInput = input.replace(" ", "").lowercase()
        return correctAnswers.any {
            it.replace(" ", "").lowercase() == formattedInput
        }
    }
}