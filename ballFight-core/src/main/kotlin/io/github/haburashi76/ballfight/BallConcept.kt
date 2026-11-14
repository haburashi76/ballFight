package io.github.haburashi76.ballfight

import io.github.haburashi76.ballfight.ability.Ability
import javafx.scene.image.Image

abstract class BallConcept {

    lateinit var abilities: List<Ability>

    var displayName: String = ""
        protected set

    var movementSpeed = 100.0

    lateinit var ball: Ball
        private set

    var maxHealth = 100

    lateinit var icon: Image

    internal fun createBallInstance(): Ball {
        return Ball().apply {
            initConcept(this@BallConcept)
            ball = this
        }
    }

}