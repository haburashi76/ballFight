package io.github.haburashi76.ballfight.ability.effect

import io.github.haburashi76.ballfight.BallFightApplication
import io.github.haburashi76.ballfight.labelFont
import io.github.haburashi76.ballfight.utils.Vector
import javafx.application.Platform
import javafx.scene.control.Label
import javafx.scene.image.Image
import javafx.scene.paint.Color
import javafx.scene.paint.ImagePattern
import javafx.scene.shape.Circle
import javafx.scene.shape.Shape

abstract class Effect(
    val duration: Long
) {
    open val obj: Shape? = null

    var isValid = true
        protected set

    var ticks = 0L

    private lateinit var location: Vector

    abstract fun update()

    internal open fun play(location: Vector) {
        this.location = location
        EffectManager.play(this)
    }

    companion object {
        private val EXPLOSION_IMAGE =
            ImagePattern(Image(BallFightApplication::class.java.getResourceAsStream("/explosion.png")))

        val EXPLOSION
            get() = object : Effect(25L) {
                override val obj = Circle(50.0).apply {
                    fill = EXPLOSION_IMAGE
                    strokeWidth = 0.0
                }

                init {
                    EffectManager.pane.children.add(obj)
                }

                override fun update() {
                    ticks++
                    obj.radius += 4.0
                    obj.opacity -= 0.04
                    if (ticks >= duration) {
                        Platform.runLater {
                            EffectManager.pane.children.remove(obj)
                            isValid = false
                        }
                    }
                }

                override fun play(location: Vector) {
                    super.play(location)
                    obj.centerX = location.x
                    obj.centerY = location.y
                }
            }
        val BIG_EXPLOSION
            get() = object : Effect(35L) {
                override val obj = Circle(150.0).apply {
                    fill = EXPLOSION_IMAGE
                    strokeWidth = 0.0
                }

                init {
                    EffectManager.pane.children.add(obj)
                }

                override fun update() {
                    ticks++
                    obj.radius += 9.0
                    obj.opacity -= 0.04
                    if (ticks >= duration) {
                        Platform.runLater {
                            EffectManager.pane.children.remove(obj)
                            isValid = false
                        }
                    }
                }

                override fun play(location: Vector) {
                    super.play(location)
                    obj.centerX = location.x
                    obj.centerY = location.y
                }
            }
        val SHAKE
            get() = object : Effect(10L) {

                val sign = listOf(1.0, -1.0)

                val y_direction = sign.random()

                override fun update() {
                    ticks++

                    EffectManager.pane.parent.translateY = y_direction * 0.35 * ticks * (ticks - duration)

                    if (ticks >= duration) {
                        isValid = false
                    }

                }
            }

        fun text(text: String, color: Color = Color.WHITE): Effect {
            return object : Effect(30L) {
                val label = Label(text).apply {
                    textFill = color
                    font = labelFont
                }

                init {
                    Platform.runLater {
                        EffectManager.pane.children.add(label)
                    }
                }

                override fun update() {
                    ticks++
                    label.opacity -= 0.04
                    label.translateY -= 1.0
                    if (ticks >= duration) {
                        Platform.runLater {
                            EffectManager.pane.children.remove(label)
                            isValid = false
                        }
                    }
                }
                override fun play(location: Vector) {
                    super.play(location)
                    label.translateX = location.x
                    label.translateY = location.y
                }
            }
        }
    }
}
