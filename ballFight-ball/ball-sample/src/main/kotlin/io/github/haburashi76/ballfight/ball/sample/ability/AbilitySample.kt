package io.github.haburashi76.ballfight.ball.sample.ability

import io.github.haburashi76.ballfight.Ball
import io.github.haburashi76.ballfight.ability.PassiveAbility
import io.github.haburashi76.ballfight.events.BallCollideByBallEvent
import io.github.haburashi76.ballfight.events.BallDamageEvent
import io.github.haburashi76.ballfight.events.EventHandler
import io.github.haburashi76.ballfight.events.EventReceiver

@Suppress("UNUSED")
class AbilitySample: PassiveAbility() {
    override val name: String = "샘플"

    @EventHandler(receiver = BallCollideByBallReceiver::class)
    fun onDamage(event: BallCollideByBallEvent) {
        event.with.damage(5, ball)
    }
}
class BallCollideByBallReceiver: EventReceiver<BallCollideByBallEvent> {
    override fun getReceiver(event: BallCollideByBallEvent): Ball {
        return event.ball
    }
}