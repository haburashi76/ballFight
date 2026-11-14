package io.github.haburashi76.ballfight.ability.effect

import javafx.scene.layout.Pane

object EffectManager {
    lateinit var pane: Pane

    val effects = mutableListOf<Effect>()

    fun play(effect: Effect) {
        effects += effect
    }

    internal fun update() {
        effects.toMutableList().forEach { it.update() }
        effects.removeIf { !it.isValid }
    }
}