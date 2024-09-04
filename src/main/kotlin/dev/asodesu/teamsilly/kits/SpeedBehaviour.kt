package dev.asodesu.teamsilly.kits

import dev.asodesu.origami.engine.player.PlayerBehaviour
import dev.asodesu.origami.engine.player.PlayerBehaviourContainer
import dev.asodesu.origami.engine.wiring.annotations.Tick
import dev.asodesu.origami.utilities.bukkit.addPotionEffect
import org.bukkit.potion.PotionEffectType
import kotlin.time.Duration.Companion.days

class SpeedBehaviour(c: PlayerBehaviourContainer) : PlayerBehaviour(c) {

    fun postApply() {
        player.addPotionEffect(PotionEffectType.SPEED, duration = 99999.days, amplifier = 3, icon = true)
    }

}