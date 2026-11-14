package io.github.haburashi76.ballfight

import io.github.haburashi76.ballfight.events.BallCollideByBallEvent
import io.github.haburashi76.ballfight.events.MatchStartEvent
import io.github.haburashi76.ballfight.tick.TickManager
import io.github.haburashi76.ballfight.utils.Vector
import io.github.haburashi76.ballfight.utils.minus
import io.github.haburashi76.ballfight.utils.plus
import kotlin.math.sqrt


class Match(val balls: Pair<Ball, Ball>) {
    var isRunning = false
        private set

    var winner: Ball? = null

    fun start() {
        isRunning = true
        MatchStartEvent(this).callEvent(this)
    }
    fun stop() {
        isRunning = false
    }

    private var lastCollide = 0L

    fun checkBallCollisions() {
        val first = balls.first
        val second = balls.second

        if (!first.isAlive || !second.isAlive) return

        if (isColliding(first, second)) {
            collideBalls(first, second)
        }
    }

    private fun isColliding(
        first: Ball,
        second: Ball
    ): Boolean {
        val xDifference = second.location!!.x - first.location!!.x
        val yDifference = second.location!!.y - first.location!!.y

        val distanceSquared =
            xDifference * xDifference +
                    yDifference * yDifference

        val collisionDistance =
            100.0

        return distanceSquared <=
                collisionDistance * collisionDistance
    }

    private fun collideBalls(
        first: Ball,
        second: Ball
    ) {
        val firstOriginSpeed = first.velocity!!.length
        val secondOriginSpeed = second.velocity!!.length

        val xDifference = second.location!!.x - first.location!!.x
        val yDifference = second.location!!.y - first.location!!.y

        val distance = sqrt(
            xDifference * xDifference +
                    yDifference * yDifference
        )

        if (distance == 0.0) {
            return
        }

        val normalX = xDifference / distance
        val normalY = yDifference / distance

        val overlap =
            100.0 - distance

        if (overlap > 0.0) {
            val correction = overlap / 2.0

            first.location = first.location!! - Vector(
                normalX * correction,
                normalY * correction
            )

            second.location = second.location!! + Vector(
                normalX * correction,
                normalY * correction
            )
        }

        val relativeX =
            first.velocity!!.x -
                    second.velocity!!.x

        val relativeY =
            first.velocity!!.y -
                    second.velocity!!.y


        val relativeSpeed =
            relativeX * normalX +
                    relativeY * normalY

        if (relativeSpeed <= 0.0) {
            return
        }

        val firstOldVelocity =
            first.velocity

        val secondOldVelocity =
            second.velocity


        first.velocity = Vector(
            first.velocity!!.x -
                    relativeSpeed * normalX,
            first.velocity!!.y -
                    relativeSpeed * normalY
        )
        first.velocity!!.setLength(firstOriginSpeed)

        second.velocity = Vector(
            second.velocity!!.x +
                    relativeSpeed * normalX,
            second.velocity!!.y +
                    relativeSpeed * normalY
        )
        second.velocity!!.setLength(secondOriginSpeed)

        if (lastCollide + 5L < TickManager.currentTicks) {
            lastCollide = TickManager.currentTicks
            BallCollideByBallEvent(
                first,
                second,
                firstOldVelocity!!,
                first.velocity!!
            ).callEvent(this)

            BallCollideByBallEvent(
                second,
                first,
                secondOldVelocity!!,
                second.velocity!!
            ).callEvent(this)
        }
    }
}