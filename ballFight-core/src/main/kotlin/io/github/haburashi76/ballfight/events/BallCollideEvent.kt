package io.github.haburashi76.ballfight.events

import io.github.haburashi76.ballfight.Ball
import io.github.haburashi76.ballfight.utils.Vector

open class BallCollideEvent(
    override val ball: Ball,
    open val oldDirection: Vector,
    open val newDirection: Vector
): BallEvent