package dev.asodesu.teamsilly.utils

import net.kyori.adventure.audience.Audience
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player

fun Player.reset() {
    this.clearActivePotionEffects()
    this.inventory.clear()
    this.exp = 0f
    this.level = 0
    this.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)?.baseValue = 0.1
    this.health = this.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.value ?: 20.0
    this.foodLevel = 20
    this.flySpeed = 0.2f
}

fun Iterable<Player>.audience() = Audience.audience(this)