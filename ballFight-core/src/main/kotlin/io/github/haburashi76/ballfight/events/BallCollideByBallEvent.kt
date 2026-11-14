package io.github.haburashi76.ballfight.events

import io.github.haburashi76.ballfight.Ball
import io.github.haburashi76.ballfight.utils.Vector

class BallCollideByBallEvent(
    override val ball: Ball,
    val with: Ball,
    override val oldDirection: Vector,
    override val newDirection: Vector
): BallCollideEvent(
    ball,
    oldDirection,
    newDirection
)