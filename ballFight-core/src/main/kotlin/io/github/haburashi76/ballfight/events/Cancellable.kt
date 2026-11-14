package io.github.haburashi76.ballfight.events

interface Cancellable {
    fun cancel()
    var isCancelled: Boolean
}