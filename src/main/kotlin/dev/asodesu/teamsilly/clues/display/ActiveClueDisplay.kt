package dev.asodesu.teamsilly.clues.display

import dev.asodesu.origami.engine.Destroyable
import dev.asodesu.origami.utilities.bukkit.off
import dev.asodesu.origami.utilities.miniMessage
import dev.asodesu.teamsilly.clues.Clue
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.entity.Display
import org.bukkit.entity.Entity
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.TextDisplay
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Transformation
import org.joml.AxisAngle4f
import org.joml.Vector3f

class ActiveClueDisplay(val origin: Location, val clue: Clue) : ClueDisplay {
    val clueDisplayWidth = 3.0
    val clueDisplayHeight = 2.0

    val center = clueDisplayWidth / 2

    val entities = mutableListOf<Entity>()

    init {
        text(center, 0.4, "<b><gold>${clue.name}", scale = 0.9f, background = false)
        text((center / 2) + 0.55, 1.05, """
            <font:asodesu:pixel>TYPE: <gray>${clue.puzzle.name}</gray>
            REQUIRES KIT: <gray>${clue.puzzle.kitRequirement}
            <font:asodesu:padding>a
        """.trimIndent(), scale = 0.8f, background = false)

        text((center / 2) + 0.4, 1.7, """
            <font:asodesu:pixel><gray>${clue.description}
        """.trimIndent(), scale = 0.8f, background = false, width = 100)

        item(
            center + 1.0,
            1.5,
            clue.item,
            scale = 0.75f,
            axisAngle4f = AxisAngle4f(Math.toRadians(-15.0).toFloat(), 0f, 0f, 1f)
        )
    }

    override fun update() {

    }

    override fun destroy() {
        entities.forEach { it.remove() }
    }

    fun text(x: Double, y: Double, text: String, scale: Float = 1f, background: Boolean = true, width: Int = -1) {
        val pos = pos(x, y)
        pos.world.spawn(pos, TextDisplay::class.java) {
            it.text(miniMessage(text))
            it.transformation = Transformation(
                Vector3f(),
                AxisAngle4f(Math.toRadians(90.0).toFloat(), 0f, 1f, 0f),
                Vector3f(scale),
                AxisAngle4f()
            )
            if (width > 0) it.lineWidth = width
            it.alignment = TextDisplay.TextAlignment.LEFT
            it.brightness = Display.Brightness(15, 15)
            if (!background) it.backgroundColor = Color.fromARGB(0, 0, 0, 0)
            entities += it
        }
    }
    fun item(x: Double, y: Double, item: ItemStack, scale: Float = 1f, axisAngle4f: AxisAngle4f = AxisAngle4f()) {
        val pos = pos(x, y)
        pos.world.spawn(pos, ItemDisplay::class.java) {
            it.setItemStack(item)
            it.transformation = Transformation(
                Vector3f(),
                AxisAngle4f(Math.toRadians(90.0).toFloat(), 0f, 1f, 0f),
                Vector3f(scale),
                axisAngle4f
            )
            entities += it
        }
    }

    fun pos(x: Double, y: Double) = origin.toBlockLocation().off(x = 0.01, y = -y + 1, z = -x + 1)
}