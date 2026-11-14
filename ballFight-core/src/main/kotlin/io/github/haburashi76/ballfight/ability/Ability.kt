package io.github.haburashi76.ballfight.ability

import io.github.haburashi76.ballfight.Ball
import io.github.haburashi76.ballfight.BallConcept
import io.github.haburashi76.ballfight.ability.effect.Effect
import io.github.haburashi76.ballfight.utils.Vector
import javafx.scene.media.Media
import javafx.scene.paint.Color
import kotlin.reflect.KProperty0

abstract class Ability {
    abstract val name: String

    lateinit var ballConcept: BallConcept

    lateinit var ball: Ball

    internal val progressBars: MutableMap<Pair<KProperty0<Double>, Double>, Color> = mutableMapOf()

    fun initialize(concept: BallConcept, ball: Ball) {
        this.ballConcept = concept
        this.ball = ball
    }

    fun launchProjectile(projectile: Projectile, location: Vector) {
        projectile.init(location)
        projectile.launched = true
        projectile.shooter = ball
        ProjectileManager.register(projectile)
        projectile.onLaunched()
    }

    fun playEffect(effect: Effect, location: Vector) {
        effect.play(location)
    }

    fun playSound(media: Media, volume: Double) {
        SoundManager.playSound(media, volume)
    }

    fun registerProgressBar(property: KProperty0<Double>, maxValue: Double, color: Color) {
        progressBars[property to maxValue] = color
    }

    open fun onEnable() {}
}