package dev.asodesu.teamsilly.behaviour

import dev.asodesu.origami.engine.player.PlayerBehaviour
import dev.asodesu.origami.engine.player.PlayerBehaviourContainer
import dev.asodesu.teamsilly.kits.Kits

class KitBehaviour(c: PlayerBehaviourContainer, val kit: Kits.Kit) : PlayerBehaviour(c) {
}