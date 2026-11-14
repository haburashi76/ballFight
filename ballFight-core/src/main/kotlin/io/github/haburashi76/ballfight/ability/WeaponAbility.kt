package io.github.haburashi76.ballfight.ability

import io.github.haburashi76.ballfight.Ball
import io.github.haburashi76.ballfight.view.BattleScene.Companion.ballViews
import javafx.scene.paint.ImagePattern
import javafx.scene.paint.Paint
import javafx.scene.shape.Rectangle
import javafx.scene.shape.Shape
import javafx.scene.transform.Rotate

interface WeaponAbility {
    var weapon: Weapon?
    fun onWeaponHit(hit: Ball)
}
class Weapon(
    val ball: Ball,
    val ability: WeaponAbility,
    val paint: Paint,
    height: Double,
    width: Double,
    initialRotate: Double = 0.0,
    var speed: Double = 240.0
) {
    var hit: Ball? = null

    var height = height
        set(value) {
            field = value
            rectangle.height = field
            if (paint is ImagePattern) {
                rectangle.fill = ImagePattern(
                    paint.image
                )
            }
        }

    var width = width
        set(value) {
            field = value
            rectangle.width = field
            if (paint is ImagePattern) {
                rectangle.fill = ImagePattern(
                    paint.image)
            }
        }

    val rectangle = Rectangle(width, height, paint)

    val rotate = Rotate(initialRotate, ball.location!!.x, ball.location!!.y)

    init {
        rectangle.transforms.add(rotate)
        rectangle.x = ball.location!!.x - width * 0.5
        rectangle.y = ball.location!!.y - height
    }

    fun update() {
        rectangle.x = ball.location!!.x - width * 0.5
        rectangle.y = ball.location!!.y - height

        rotate.pivotX = ball.location!!.x
        rotate.pivotY = ball.location!!.y

        rotate.angle += speed / 60

        val check = checkHit()

        if (check) {
            if (hit == null) {
                hit = ball.opposite!!
                if (hit!!.buffMap["disappear"] == null && ball.buffMap["disappear"] == null) {
                    ability.onWeaponHit(hit!!)
                }
            }
        } else {
            hit = null
        }
    }

    private fun checkHit(): Boolean {
        val intersection = Shape.intersect(rectangle, ballViews[ball.opposite])
        return intersection.boundsInLocal.width > 0 &&
                intersection.boundsInLocal.height > 0
    }
}