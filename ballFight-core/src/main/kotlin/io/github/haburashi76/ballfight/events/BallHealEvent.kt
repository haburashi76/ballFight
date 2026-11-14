package io.github.haburashi76.ballfight.events

import io.github.haburashi76.ballfight.Ball

class BallHealEvent(
    override val ball: Ball,
    val healer: Ball?,
    var amount: Int
): BallEvent, Cancellable {
    override var isCancelled: Boolean = false

    override fun cancel() {
        isCancelled = true
    }
}