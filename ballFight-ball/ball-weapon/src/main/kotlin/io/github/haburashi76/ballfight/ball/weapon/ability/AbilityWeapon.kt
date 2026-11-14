package io.github.haburashi76.ballfight.ball.weapon.ability

import io.github.haburashi76.ballfight.Ball
import io.github.haburashi76.ballfight.ability.PassiveAbility
import io.github.haburashi76.ballfight.ability.Weapon
import io.github.haburashi76.ballfight.ability.WeaponAbility
import javafx.scene.paint.ImagePattern

@Suppress("UNUSED")
class AbilityWeapon: PassiveAbility(), WeaponAbility {
    override val name: String = "무기"

    override var weapon: Weapon? = Weapon(
        ball,
        this,
        ImagePattern(ball.getResourceImage("sword.png")),
        80.0,
        10.0
    )

    override fun onWeaponHit(hit: Ball) {
        hit.damage(5, ball)
    }
}