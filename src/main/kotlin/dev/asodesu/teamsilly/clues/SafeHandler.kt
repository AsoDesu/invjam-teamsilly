package dev.asodesu.teamsilly.clues

import dev.asodesu.origami.engine.Behaviour
import dev.asodesu.origami.engine.consumeCooldown
import dev.asodesu.origami.engine.wiring.annotations.Subscribe
import dev.asodesu.origami.utilities.bukkit.off
import dev.asodesu.origami.utilities.bukkit.particle
import dev.asodesu.origami.utilities.bukkit.runLater
import dev.asodesu.origami.utilities.error
import dev.asodesu.origami.utilities.play
import dev.asodesu.origami.utilities.sendTitle
import dev.asodesu.teamsilly.KEY_CLUEID
import dev.asodesu.teamsilly.SillyGamePlugin
import dev.asodesu.teamsilly.build.MapData
import dev.asodesu.teamsilly.build.element.all
import dev.asodesu.teamsilly.build.element.noRotation
import dev.asodesu.teamsilly.build.element.resolveSingle
import dev.asodesu.teamsilly.build.element.single
import dev.asodesu.teamsilly.build.element.withAttribute
import dev.asodesu.teamsilly.game.SillyGameScene
import dev.asodesu.teamsilly.utils.SOUND_SAFE_ACTIVATE
import dev.asodesu.teamsilly.utils.SOUND_SAFE_FAIL
import dev.asodesu.teamsilly.utils.SOUND_SAFE_FAIL_LOCK
import dev.asodesu.teamsilly.utils.SOUND_SAFE_OPEN
import dev.asodesu.teamsilly.utils.SOUND_SAFE_RISER
import org.bukkit.Color
import org.bukkit.FireworkEffect
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.entity.Firework
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.util.Vector
import kotlin.time.Duration.Companion.seconds

class SafeHandler(val id: String, val clue: Clue, scene: SillyGameScene) : Behaviour() {
    val safeBlockElement = scene.mapData.positions.all("safe_block").withAttribute("clue_id", id).single()

    val direction = BlockFace.valueOf((safeBlockElement.attributes["facing"] ?: "west").uppercase())
    val safeBlock = safeBlockElement.resolve(scene.world).noRotation()

    fun postApply() {
        safeBlock.block.type = Material.LODESTONE
    }

    @Subscribe
    fun interact(evt: PlayerInteractEvent) {
        val clickedBlock = evt.clickedBlock ?: return
        if (clickedBlock.location.toBlockLocation() != safeBlock.toBlockLocation()) return
        val player = evt.player
        evt.isCancelled = true

        val item = evt.item ?: return
        val clueId = item.persistentDataContainer.get(KEY_CLUEID, PersistentDataType.STRING)
        if (clueId != id) {
            evt.player.swingHand(evt.hand ?: EquipmentSlot.HAND)
            if (evt.player.consumeCooldown("safe_handler", 0.5.seconds)) return
            player.error("That is not the key to the safe.")
            player.play(SOUND_SAFE_FAIL)
            player.play(SOUND_SAFE_FAIL_LOCK)
            return
        }

        val originOffset = Vector(0.55, 0.0, 0.55).multiply(direction.direction)
        val particleOrigin = safeBlock.toCenterLocation()
            .off(y = 1.25)
            .off(x = originOffset.x, z = originOffset.z)
        safeBlock.world.particle(
            type = Particle.VAULT_CONNECTION,
            location = particleOrigin,
            count = 1000,
            offset = Vector(0.05, 0.05, 0.05),
            3.0
        )

        item.subtract(1)
        player.swingHand(evt.hand ?: EquipmentSlot.HAND)
        player.play(SOUND_SAFE_ACTIVATE)
        player.play(SOUND_SAFE_RISER)
        runLater(2.5.seconds) {
            particleOrigin.world.spawn(particleOrigin.off(y = -1.25), Firework::class.java) { firework ->
                firework.velocity = Vector(1.0, 0.0, 1.0).multiply(direction.direction)
                firework.fireworkMeta = firework.fireworkMeta.also { meta ->
                    meta.addEffects(
                        FireworkEffect.builder().with(FireworkEffect.Type.BURST).withColor(Color.LIME).build()
                    )
                }
                firework.isShotAtAngle = true
                firework.detonate()
            }

            player.play("entity.item.pickup")
            player.play(SOUND_SAFE_OPEN)
            player.inventory.addItem(clue.item)
            player.sendTitle(subtitle = "<aqua><obf>[]</obf></aqua> Obtained <green>${clue.name} <red><obf>[]</obf></red>")
        }
        //this.destroy()
    }
}