package io.github.haburashi76.ballfight.events

import io.github.haburashi76.ballfight.Match

@Suppress("UNCHECKED_CAST")
interface Event {
    fun callEvent(match: Match): Boolean {
        try {
            match.balls.toList().forEach { ball ->
                ball.concept.abilities.forEach { ability ->
                    val methods = ability::class.java.declaredMethods
                        .filter { it.isAnnotationPresent(EventHandler::class.java) }
                        .filter {
                            it.parameterTypes.single().isAssignableFrom(this::class.java)
                                    && (it.getAnnotation(EventHandler::class.java).receiver.java
                                .getConstructor().newInstance() as EventReceiver<Event>)
                                .getReceiver(this) == ball
                        }
                    methods.forEach{ it.invoke(ability, this) }
                }
            }
            return this !is Cancellable || !this.isCancelled
        } catch (e: Exception) {
            println("Cannot call the event: ${this::class.java.name}")
            e.printStackTrace()
        }
        return false
    }
}