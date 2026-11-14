package io.github.haburashi76.ballfight.events

import kotlin.reflect.KClass

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class EventHandler(val receiver: KClass<out EventReceiver<*>>)