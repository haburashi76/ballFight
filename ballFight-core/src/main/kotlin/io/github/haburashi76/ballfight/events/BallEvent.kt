package io.github.haburashi76.ballfight.events

import io.github.haburashi76.ballfight.Ball

interface BallEvent: Event {
    val ball: Ball
}