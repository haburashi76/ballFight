package io.github.haburashi76.ballfight

import java.io.File

class BallContainer(
    val file: File,
    val ballConceptClass: Class<*>,
    val abilityClasses: List<Class<*>>,
    val ballDescription: BallDescription
)