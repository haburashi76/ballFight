package io.github.haburashi76.ballfight.ability

abstract class ActiveAbility: Ability() {
    abstract val conceptCooldown: Long
    open val displayCooldown: Boolean = false
    var cooldown = 0L
    open fun onReady() {}
}