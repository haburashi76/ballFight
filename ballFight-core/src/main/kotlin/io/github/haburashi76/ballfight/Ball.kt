package io.github.haburashi76.ballfight

import io.github.haburashi76.ballfight.ability.effect.Effect
import io.github.haburashi76.ballfight.events.BallCollideEvent
import io.github.haburashi76.ballfight.events.BallDamageEvent
import io.github.haburashi76.ballfight.events.BallHealEvent
import io.github.haburashi76.ballfight.tick.TickManager
import io.github.haburashi76.ballfight.utils.Vector
import io.github.haburashi76.ballfight.utils.div
import io.github.haburashi76.ballfight.utils.times
import io.github.haburashi76.ballfight.view.BattleScene
import javafx.scene.image.Image
import javafx.scene.media.Media
import java.io.File
import java.util.UUID
import java.util.jar.JarFile
import kotlin.math.PI
import kotlin.random.Random

class Ball {
    lateinit var file: File

    private val tasks: MutableList<UUID> = mutableListOf()

    private var match: Match? = null

    val opposite: Ball?
        get() = match?.let { it.balls.toList().single { ball -> ball != this } }

    lateinit var concept: BallConcept
        private set

    var health: Int = 0

    var location: Vector? = null

    var velocity: Vector? = null

    val isAlive: Boolean
        get() { return health >= 0 }

    val buffMap = mutableMapOf<String, Long>()

    fun getResourceImage(name: String): Image {
        JarFile(file).use { jar ->
            jar.getJarEntry(name)?.let { entry ->
                jar.getInputStream(entry).use {
                    return Image(it)
                }
            }
        }

        error("Failed to open JarFile ${file.name}")
    }

    fun getResourceMedia(name: String): Media {
        JarFile(file).use {
            return Media("jar:${file.toURI()}!/$name")
        }
    }

    fun initConcept(concept: BallConcept) {
        this.concept = concept
    }

    fun setUp(match: Match) {
        this.match = match
        health = concept.maxHealth
        val angle = Random.nextDouble(0.0, 2 * PI)
        velocity = Vector(angle).multiply(concept.movementSpeed)
    }

    internal fun move() {
        if (location != null && velocity != null) {

            var testLocation = location!!.clone().add(velocity!!*2/BattleScene.currentFPS)

            if (testLocation.x !in (50.0..550.0)) {
                BallCollideEvent(
                    this, velocity!!.clone().normalize(),
                    velocity!!.multiply(Vector(-1.0, 1.0)
                    )
                ).callEvent(match!!)
                testLocation = location!!.clone().add(velocity!!/60.0)
            }
            if (testLocation.y !in (50.0..670.0)) {
                BallCollideEvent(
                    this, velocity!!.clone().normalize(),
                    velocity!!.multiply(Vector(1.0, -1.0)
                    )
                ).callEvent(match!!)
                testLocation = location!!.clone().add(velocity!!/60.0)
            }
            location = Vector(
                testLocation.x.coerceIn(50.0, 550.0),
                testLocation.y.coerceIn(50.0, 670.0)
            )
        }
    }

    fun knockBack(velocity: Vector, time: Long) {
        this.velocity = velocity
        runTask(time) {
            this.velocity!!.setLength(concept.movementSpeed)
        }
    }

    fun heal(amount: Int, healer: Ball?) {
        val event = BallHealEvent(this, healer, amount)
        if (match != null)
        if (event.callEvent(match!!)) {
            health = (health + event.amount).coerceAtMost(concept.maxHealth)
        }
    }

    fun damage(amount: Int, damager: Ball) {
        val event = BallDamageEvent(this, damager, amount)
        if (match != null)
        if (event.callEvent(match!!)) {
            health = (health - event.amount).coerceAtLeast(0)
        }
    }

    fun runTaskTimer(delay: Long, period: Long, runnable: Runnable): UUID {
        val id = TickManager.runTaskTimer(runnable, delay, period)
        tasks.add(id)
        return id
    }

    fun runTask(delay: Long, runnable: Runnable) {
        TickManager.runTask(runnable, delay)
    }

    fun stopTask(id: UUID) {
        tasks.remove(id)
        TickManager.unRegister(id)
    }

    fun disappear(ticks: Long) {
        buffMap += "disappear" to ticks
    }

    fun playEffect(effect: Effect, location: Vector) {
        effect.play(location)
    }
}