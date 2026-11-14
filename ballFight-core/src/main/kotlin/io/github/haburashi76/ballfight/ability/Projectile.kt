package io.github.haburashi76.ballfight.ability

import io.github.haburashi76.ballfight.Ball
import io.github.haburashi76.ballfight.utils.Vector
import io.github.haburashi76.ballfight.utils.div
import io.github.haburashi76.ballfight.utils.times
import io.github.haburashi76.ballfight.view.BattleScene
import io.github.haburashi76.ballfight.view.BattleScene.Companion.currentFPS
import io.github.haburashi76.ballfight.view.BattleScene.Companion.projectileShapes
import javafx.application.Platform
import javafx.scene.paint.Paint

abstract class Projectile(val range: Double) {
    abstract val fill: Paint?

    abstract val strokeWidth: Double

    lateinit var shooter: Ball

    var hitRange = 10.0

    var isValid = true

    var velocity: Vector = Vector(0.0, 0.0)

    var launched: Boolean = false
        internal set

    var movedDistance = 0.0
        private set

    var location = Vector(0.0, 0.0)
        private set

    val shape
        get() = projectileShapes[this]

    internal fun init(location: Vector) {
        this.location = location
    }

    open fun hittable(): Ball? {
        return shooter.opposite
    }

    internal fun update() {
        if (!isValid) {
            remove()
            return
        }

        if (velocity.length == 0.0) {
            remove()
            return
        }

        val currentLocation = location.clone()
        val targetLocation = location.clone()

        val remainRange = range - movedDistance

        val velocity = velocity.clone()
        var speed = velocity.length

        if (movedDistance >= range) {
            remove()
            return
        } else if (remainRange < speed * 2 / currentFPS) {
            speed = remainRange
            velocity.setLength(speed * currentFPS / 2)
        }

        targetLocation.add(velocity * 2.005 / currentFPS)

        val movement = Movement(currentLocation, targetLocation)

        onMove(movement)

        location = movement.to

        movedDistance += movement.from.distance(movement.to)

        val hit = hitCheck()

        if (hit != null) {
            onHit(hit)
        }
    }

    private fun hitCheck(): HitResult? {
        val hittable = hittable()?: return null
        val distance = hittable.location?.distance(location)?: return null
        if (distance <= 50.0 + hitRange && hittable.buffMap["disappear"] == null) {
            return HitResult(hittable)
        }
        return null
    }

    protected open fun onMove(movement: Movement) {}

    protected open fun onHit(result: HitResult) {}

    protected open fun onRemove() {}

    open fun onLaunched() {}

    fun remove() {
        if (!isValid) return
        isValid = false
        onRemove()
    }

    class Movement(val from: Vector, val to: Vector)

    class HitResult(val hitBalls: Ball)
}