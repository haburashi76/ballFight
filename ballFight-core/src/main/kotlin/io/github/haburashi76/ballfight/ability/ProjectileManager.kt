package io.github.haburashi76.ballfight.ability

import io.github.haburashi76.ballfight.Match
import javafx.scene.layout.Pane

object ProjectileManager {
    lateinit var match: Match
    val projectiles = mutableListOf<Projectile>()
    lateinit var pane: Pane

    fun register(projectile: Projectile) {
        projectiles.add(projectile)
    }
}