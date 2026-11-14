package io.github.haburashi76.ballfight.events

import io.github.haburashi76.ballfight.Ball

interface EventReceiver<T: Event> {
    fun getReceiver(event: T): Ball?
    class BallDamage {
        class Damager: EventReceiver<BallDamageEvent> {
            override fun getReceiver(event: BallDamageEvent): Ball? {
                return event.damager
            }
        }
        class Victim: EventReceiver<BallDamageEvent> {
            override fun getReceiver(event: BallDamageEvent): Ball? {
                return event.ball
            }
        }
    }
}