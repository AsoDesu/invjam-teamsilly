package dev.asodesu.teamsilly.clues.puzzles

import dev.asodesu.origami.engine.add
import dev.asodesu.origami.engine.checkCooldown
import dev.asodesu.origami.engine.consumeCooldown
import dev.asodesu.origami.engine.player.PlayerBehaviourContainer
import dev.asodesu.origami.engine.player.container
import dev.asodesu.origami.engine.replace
import dev.asodesu.origami.engine.wiring.annotations.Subscribe
import dev.asodesu.origami.utilities.send
import dev.asodesu.origami.utilities.warning
import dev.asodesu.teamsilly.behaviour.ChatPromptBehaviour
import dev.asodesu.teamsilly.behaviour.DialogueBehaviour
import dev.asodesu.teamsilly.build.MapData
import dev.asodesu.teamsilly.build.element.all
import dev.asodesu.teamsilly.clues.CluePuzzle
import org.bukkit.NamespacedKey
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.persistence.PersistentDataType
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds

class MathsPuzzle(id: String, val world: World, mapData: MapData) : CluePuzzle(id, mapData, world) {
    override val name: String = "Math"
    val triggerRegion = mapData.boundingBoxes.all("trivia_npc_trigger").withPuzzleId().single().box

    val npcIdKey = NamespacedKey("asodesu", "npc_id")
    val npcId = "math_teacher"

    val questions = listOf(
        Question(
            lines = listOf(
                "<gold>[NPC] Ms Math Teacher</gold>: Aha, you must be one of my new students!" to 3.seconds,
                "<gold>[NPC] Ms Math Teacher</gold>: Of course, only the top students have access to the super suspicious industrial vault over there..." to 5.seconds,
                "<gold>[NPC] Ms Math Teacher</gold>: So you'll have to prove yourself <i>worthy</i>." to 3.5.seconds,
                "<gold>[NPC] Ms Math Teacher</gold>: Ready to begin?" to 3.5.seconds,

                "\n<gold>[NPC] Ms Math Teacher</gold>: Question 1: What is <b>(-2) x (-2)</b>? <gray>(Type answer in chat)" to 0.seconds,
            ),
            answers = listOf("4")
        ),
        Question(
            lines = listOf(
                "<gold>[NPC] Ms Math Teacher</gold>: Correct!" to 2.seconds,
                "\n<gold>[NPC] Ms Math Teacher</gold>: Next Question, <b>What is the square root of 169</b>? <gray>(Type answer in chat)" to 0.seconds,
            ),
            answers = listOf("13")
        ),
        Question(
            lines = listOf(
                "<gold>[NPC] Ms Math Teacher</gold>: Excellent!" to 2.seconds,
                "\n<gold>[NPC] Ms Math Teacher</gold>: Next Question, <b>What is the value of y in y² - 6y + 9 = 0</b>? <gray>(Type answer in chat)" to 0.seconds,
            ),
            answers = listOf("3")
        ),
        Question(
            lines = listOf(
                "<gold>[NPC] Ms Math Teacher</gold>: Incredible!" to 3.seconds,
                "<gold>[NPC] Ms Math Teacher</gold>: Now the final, and hardest question..." to 3.seconds,
                "\n<gold>[NPC] Ms Math Teacher</gold>: <b>What is 1+1</b>? <gray>(Type answer in chat)" to 0.seconds,
            ),
            answers = listOf("2")
        ),
        Question(
            lines = listOf(
                "<gold>[NPC] Ms Math Teacher</gold>: Simply exemplary!!! Here, you've earned this, you're going places I'll tell ya." to 3.seconds,
            ),
            answers = listOf()
        )
    )

    @Subscribe
    fun boundCheck(evt: PlayerMoveEvent) {
        if (evt.player.checkCooldown("math/npc_trigger", 10.hours)) return
        if (triggerRegion.contains(evt.to.toVector())) {
            evt.player.send("<gold>[NPC] Ms Math Teacher</gold>: Ah! My new student!")
            evt.player.consumeCooldown("math/npc_trigger", 10.hours)
        }
    }

    var currentQuestion = 0

    @Subscribe
    fun interact(evt: PlayerInteractAtEntityEvent) {
        val targetNpc = evt.rightClicked.persistentDataContainer.get(npcIdKey, PersistentDataType.STRING)
        if (targetNpc != npcId) return

        evt.isCancelled = true

        if (currentQuestion < 0) return
        val container = evt.player.container
        if (container.has(DialogueBehaviour::class)) return
        startQuestion(evt.player, currentQuestion)
    }

    private fun startQuestion(player: Player, index: Int) {
        currentQuestion = index
        val question = questions[currentQuestion]

        val container = player.container
        container.add(scope = this.scope, instance = DialogueBehaviour(question.lines, container) {
            if (currentQuestion == questions.lastIndex) {
                currentQuestion = -1
                this.complete(player)
            } else {
                chatPrompt(container, question.answers) { startQuestion(player, ++currentQuestion) }
            }
        })
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
                player.send("<gold>[NPC] Ms Math Teacher</gold>: Not quite.. give it another go. <gray>(Type answer in chat, Type 'exit' to leave)")
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

    class Question(val lines: List<Pair<String, Duration>>, val answers: List<String>)
}