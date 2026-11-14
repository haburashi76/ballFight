package io.github.haburashi76.ballfight.view

import io.github.haburashi76.ballfight.Ball
import io.github.haburashi76.ballfight.BallFightApplication
import io.github.haburashi76.ballfight.Match
import io.github.haburashi76.ballfight.ability.*
import io.github.haburashi76.ballfight.ability.effect.Effect
import io.github.haburashi76.ballfight.ability.effect.EffectManager
import io.github.haburashi76.ballfight.tick.TickManager
import io.github.haburashi76.ballfight.utils.Vector
import io.github.haburashi76.ballfight.utils.minus
import javafx.animation.AnimationTimer
import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.application.Platform
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.ProgressBar
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.paint.ImagePattern
import javafx.scene.shape.Circle
import javafx.scene.shape.Rectangle
import javafx.scene.shape.Shape
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import javafx.stage.Stage
import javafx.util.Duration
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import kotlin.reflect.KProperty0

class BattleScene(
    private val match: Match,
    private val app: BallFightApplication,
    private val stage: Stage
) : StackPane() {

    companion object {
        const val INFO_WIDTH = 180.0

        const val WIDTH = 960.0
        const val HEIGHT = 740.0

        private const val TPS = 60

        private const val TICK_NANOS = 1_000_000_000L / TPS

        private const val BALL_RADIUS = 50.0

        var currentFPS = 0.0

        val projectileShapes = ConcurrentHashMap<Projectile, Shape>()

        val ballViews = ConcurrentHashMap<Ball, Circle>()

    }

    private var accumulatedFrame = 0L

    private var lastTime = 0L

    private var currentTPS = 0.0

    private var startTime: Long? = null

    private var accumulatedTime = 0L

    private val root = HBox().apply {
        alignment = Pos.CENTER
    }

    private val battlePane = Pane()

    private val leftPane = VBox()

    private val rightPane = VBox()

    private val countdownLabel = Label()

    private val resultLabel = Label().apply {
        font = Font.font(
            "Arial",
            FontWeight.BOLD,
            80.0
        )
        textFill = Color.WHITE

        isMouseTransparent = true

        isVisible = false
    }

    private val backToMenu = Button().apply {
        TickManager.repeatingTasks.clear()
        TickManager.oneOffTasks.clear()
        TickManager.currentTicks = 0L

        EffectManager.effects.clear()

        ProjectileManager.projectiles.clear()

        font = Font.font(
            "Arial",
            FontWeight.BOLD,
            80.0
        )
        textFill = Color.BLACK

        text = "메뉴로 돌아가기"

        setOnAction {
            stage.scene = Scene(MainMenu(app, stage), 960.0, 720.0)
        }

        isFocusTraversable = false

        isVisible = false
    }

    private val healthLabels = mutableMapOf<Ball, Label>()

    private val healthBars = mutableMapOf<Ball, ProgressBar>()

    private val cooldownProgressBar = mutableMapOf<Ball, MutableMap<ProgressBar, ActiveAbility>>()

    private val additionalProgressBar = mutableMapOf<Ball, MutableMap<ProgressBar, Pair<KProperty0<Double>, Double>>>()

    private val animationTimer = object : AnimationTimer() {
        override fun handle(now: Long) {
            if (match.isRunning) {
                if (startTime == null) startTime = now

                if (lastTime == 0L) {
                    lastTime = now - startTime!!
                    return
                }

                val elapsedTime = now - startTime!! - lastTime

                lastTime = now - startTime!!

                accumulatedTime += elapsedTime



                while (accumulatedTime >= TICK_NANOS) {

                    updateBalls()

                    updateWeapons()

                    ballEffect()

                    TickManager.update()

                    EffectManager.update()

                    accumulatedTime -= TICK_NANOS
                }

                currentTPS = TickManager.currentTicks.toDouble() / (lastTime.toDouble()/1000000000.0)
                currentFPS = accumulatedFrame / (lastTime.toDouble()/1000000000.0)

                match.checkBallCollisions()
                match.balls.toList().forEach {
                    if (it.buffMap["disappear"] == null) {
                        it.move()
                    }
                }

                updateBallViews()
                updateProjectile()
                updateProjectileView()
                checkBalls()
                match.balls.toList().forEach { ballViews[it]?.toFront() }
                EffectManager.effects.forEach { effect -> effect.obj?.toFront() }

                accumulatedFrame += 1L
            }
        }
    }

    init {

        ProjectileManager.match = match

        EffectManager.pane = battlePane

        ProjectileManager.pane = battlePane

        root.apply {
            prefWidth = WIDTH
            prefHeight = HEIGHT

            minWidth = WIDTH
            minHeight = HEIGHT

            maxWidth = WIDTH
            maxHeight = HEIGHT
        }

        prefWidth = WIDTH
        prefHeight = HEIGHT

        minWidth = WIDTH
        minHeight = HEIGHT

        maxWidth = WIDTH
        maxHeight = HEIGHT

        battlePane.prefWidth = WIDTH - INFO_WIDTH*2
        battlePane.prefHeight = HEIGHT

        battlePane.maxWidth = WIDTH - INFO_WIDTH*2
        battlePane.maxHeight = HEIGHT

        battlePane.minWidth = WIDTH - INFO_WIDTH*2
        battlePane.minHeight = HEIGHT

        battlePane.style =
            "-fx-background-color: #20242B;"

        countdownLabel.apply {
            font = Font.font(
                "Arial",
                FontWeight.BOLD,
                80.0
            )

            textFill = Color.WHITE

            isMouseTransparent = true

            isVisible = false
        }

        val clip = Rectangle().apply {
            widthProperty().bind(battlePane.widthProperty())
            heightProperty().bind(battlePane.heightProperty())
        }

        battlePane.clip = clip

        createInfoPane(
            leftPane,
            match.balls.first
        )

        createInfoPane(
            rightPane,
            match.balls.second
        )

        root.children.addAll(
            leftPane,
            battlePane,
            rightPane
        )

        children.addAll(
            root,
            countdownLabel,
            resultLabel,
            backToMenu
        )

        setAlignment(
            backToMenu,
            Pos.BOTTOM_CENTER
        )

        setAlignment(
            countdownLabel,
            Pos.CENTER
        )

        setAlignment(
            resultLabel,
            Pos.CENTER
        )

        prepareBattle()
        createBallViews()
        updateBallViews()
        updateWeapons()
        startCountdown()
        match.balls.toList().forEach { ballViews[it]?.toFront() }
        EffectManager.effects.forEach { effect -> effect.obj?.toFront() }

        animationTimer.start()
    }

    private fun ballEffect() {
        match.balls.toList().forEach { ball ->
            if (Random.nextDouble() < 0.1) {
                if (ball.buffMap["vaporeon"] != null) {
                    val loc = ball.location!!.clone().apply {
                        val x = Random.nextDouble(-55.0, 55.0)
                        val y = Random.nextDouble(-55.0, 55.0)
                        add(x, y)
                    }
                    ball.playEffect(
                        object : Effect(20) {
                            override val obj = Circle(8.0).apply {
                                fill = ImagePattern(
                                    Image(
                                        BallFightApplication::class.java.getResourceAsStream("/drop.png")
                                    )
                                )
                            }
                            init {
                                Platform.runLater {
                                    battlePane.children.add(obj)
                                }
                            }
                            override fun update() {
                                ticks++
                                if (ticks == 1L) {
                                    obj.centerX = loc.x
                                    obj.centerY = loc.y
                                }
                                obj.centerY += 0.6
                                obj.opacity += 0.05
                                if (ticks >= duration) {
                                    Platform.runLater {
                                        battlePane.children.remove(obj)
                                    }
                                }
                                obj.toFront()
                            }
                        },
                        loc
                    )
                }
            }
        }
    }

    private fun updateWeapons() {
        match.balls.toList().forEach { ball ->
            ball.concept.abilities.filterIsInstance<WeaponAbility>().forEach { ability ->
                ability.weapon!!.rectangle.isVisible = ball.buffMap["disappear"] == null
                ability.weapon!!.update()
            }
        }
    }

    private fun updateBalls() {
        match.balls.toList().forEach { ball ->
            ball.concept.abilities.forEach { ability ->
                if (ability is ActiveAbility) {
                    if (ball.buffMap["disappear"] == null) {
                        if (ability.cooldown > 0L) {
                            ability.cooldown--
                        } else {
                            ability.onReady()
                            ability.cooldown = ability.conceptCooldown
                        }
                    }
                }
            }

            ball.concept.abilities.filterIsInstance<Updatable>().forEach { ability ->
                ability.update()
            }

            val remove = mutableListOf<String>()

            ball.buffMap.forEach { (id, duration) ->
                if (duration > 0L) {
                    ball.buffMap[id] = ball.buffMap[id]!! - 1L
                } else {
                    remove.add(id)
                }
            }
            remove.forEach { ball.buffMap.remove(it) }
        }
    }

    private fun updateProjectile() {
        ProjectileManager.projectiles.toList().forEach { projectile ->
            projectile.update()

            if (projectileShapes[projectile] == null) {
                Platform.runLater {
                    val display = Circle(projectile.hitRange).apply {
                        fill = projectile.fill
                        stroke = Color.WHITE
                        strokeWidth = projectile.strokeWidth
                    }

                    projectileShapes[projectile] = display

                    battlePane.children.add(display)
                }
            }
        }
    }

    private fun updateProjectileView() {
        val remove = mutableListOf<Projectile>()
        projectileShapes.forEach { (projectile, shape) ->
            if (!projectile.isValid) {
                remove += projectile
                shape.isVisible = false
                Platform.runLater {
                    ProjectileManager.pane.children.remove(shape)
                }
            } else {
                shape.layoutX = projectile.location.x
                shape.layoutY = projectile.location.y
            }
        }
        remove.forEach {
            projectileShapes.remove(it)
            ProjectileManager.projectiles.remove(it)
        }
    }

    private fun createBallViews() {
        match.balls.toList().forEachIndexed { _, ball ->

            val circle = Circle(
                BALL_RADIUS
            )

            circle.fill = ImagePattern(ball.concept.icon)

            circle.stroke = Color.WHITE

            circle.strokeWidth = 2.0

            ballViews[ball] = circle

            battlePane.children.addAll(
                circle
            )

            ball.concept.abilities.filterIsInstance<WeaponAbility>().forEach { ability ->
                battlePane.children.addAll(
                    ability.weapon!!.rectangle
                )
            }
        }
    }

    private fun prepareBattle() {
        match.balls.toList().forEachIndexed { index, ball ->

            ball.setUp(match)

            val battleWidth = WIDTH - INFO_WIDTH*2

            when (index) {
                0 -> {
                    ball.location = Vector(battleWidth * 0.25, HEIGHT * 0.5)
                }

                1 -> {
                    ball.location = Vector(battleWidth * 0.75, HEIGHT * 0.5)
                }

                else -> {
                    ball.location = Vector(
                        Random.nextDouble(
                            BALL_RADIUS,
                            WIDTH - BALL_RADIUS
                        ),
                        Random.nextDouble(
                            BALL_RADIUS,
                            HEIGHT - BALL_RADIUS
                        )
                    )
                }
            }

            setRandomDirection(ball)

            ball.concept.abilities.forEach {
                it.onEnable()
            }
        }

        updateBallViews()
    }

    private fun setRandomDirection(
        ball: Ball
    ) {
        val angle = Random.nextDouble(
            0.0,
            Math.PI * 2
        )

        ball.velocity = Vector(
            cos(angle),
            sin(angle)
        ).multiply(ball.concept.movementSpeed)
    }

    private fun startCountdown() {
        countdownLabel.isVisible = true

        val countdown = Timeline(
            KeyFrame(
                Duration.ZERO,
                {
                    countdownLabel.text = "3"
                }
            ),

            KeyFrame(
                Duration.seconds(1.0),
                {
                    countdownLabel.text = "2"
                }
            ),

            KeyFrame(
                Duration.seconds(2.0),
                {
                    countdownLabel.text = "1"
                }
            )
            ,

            KeyFrame(
                Duration.seconds(3.0),
                {
                    countdownLabel.text = "GO!"
                    match.start()
                }
            ),

            KeyFrame(
                Duration.seconds(3.5),
                {
                    countdownLabel.isVisible = false
                }
            )
        )

        countdown.play()
    }

    private fun updateBallViews() {
        match.balls.toList().forEach { ball ->
            if (ball.buffMap["disappear"] == null) {
                ballViews[ball]?.isVisible = true
                val circle =
                    ballViews[ball]
                        ?: return@forEach

                circle.centerX = ball.location!!.x
                circle.centerY = ball.location!!.y

                circle.isVisible = ball.isAlive
            } else {
                ballViews[ball]?.isVisible = false
            }

            val healthLabel =
                healthLabels[ball]
                    ?: return@forEach

            healthLabel.text =
                "${ball.health} / ${ball.concept.maxHealth}"

            val healthBar =
                healthBars[ball]
                    ?: return@forEach

            healthBar.progress =
                (ball.health.toDouble() / ball.concept.maxHealth.toDouble()).coerceIn(0.0, 1.0)

            val healthPercent = ball.health.toDouble() / ball.concept.maxHealth.toDouble()

            healthLabel.textFill = when {
                healthPercent > 0.5 ->
                    Color.web("#32CD32")
                healthPercent > 0.2 ->
                    Color.web("#FFD700")
                else ->
                    Color.web("#FF3333")
            }

            healthBar.style = when {
                healthPercent > 0.5 ->
                    "-fx-accent: #32CD32;"
                healthPercent > 0.2 ->
                    "-fx-accent: #FFD700;"
                else ->
                    "-fx-accent: #FF3333;"
            }

            val cooldownBars = cooldownProgressBar[ball]

            cooldownBars?.forEach { (bar, ability) ->
                bar.progress = ((ability.conceptCooldown - ability.cooldown).toDouble()
                        / ability.conceptCooldown.toDouble()).coerceIn(0.0, 1.0)
            }

            val additionalBars = additionalProgressBar[ball]

            additionalBars?.forEach { (bar, pair) ->
                val property = pair.first
                val max = pair.second
                bar.progress = property.get() / max
            }
        }
    }

    private fun createInfoPane(
        pane: VBox,
        ball: Ball,
    ) {
        pane.apply {
            prefWidth = INFO_WIDTH
            prefHeight = HEIGHT

            minWidth = INFO_WIDTH
            minHeight = HEIGHT

            maxWidth = INFO_WIDTH
            minHeight = HEIGHT

            spacing = 15.0

            padding = Insets(30.0, 20.0, 20.0, 20.0)

            alignment = Pos.TOP_LEFT

            style =
                "-fx-background-color: #292E38;"
        }

        val playerLabel = Label(ball.concept.displayName)

        playerLabel.apply {
            textFill = Color.WHITE

            font = Font.font(
                "Arial",
                FontWeight.BOLD,
                27.0
            )
        }

        val healthLabel = Label()

        healthLabel.apply {
            textFill = Color.LIGHTGREEN
            font = Font.font(
                "Arial",
                FontWeight.BOLD,
                22.0
            )
        }

        healthLabels[ball] = healthLabel

        val healthBar = ProgressBar()

        healthBar.apply {
            progress = 1.0
            prefWidth = INFO_WIDTH - 40.0
            minWidth = prefWidth
            maxWidth = prefWidth
            prefHeight = 20.0
            style = "-fx-accent: #32CD32;"
        }

        healthBars[ball] = healthBar

        pane.children.addAll(
            playerLabel,
            healthLabel,
            healthBar
        )

        val cooldownMap = mutableMapOf<ProgressBar, ActiveAbility>()

        val additionalMap = mutableMapOf<ProgressBar, Pair<KProperty0<Double>, Double>>()

        ball.concept.abilities.filterIsInstance<ActiveAbility>().forEach { ability ->
            if (ability.displayCooldown) {
                val bar = ProgressBar().apply {
                    progress = ((ability.conceptCooldown.toDouble() - ability.cooldown.toDouble())
                            / ability.conceptCooldown.toDouble()
                            ).coerceIn(0.0, 1.0)
                    prefWidth = INFO_WIDTH - 40.0
                    minWidth = prefWidth
                    maxWidth = prefWidth
                    prefHeight = 20.0
                }
                pane.children.add(bar)
                cooldownMap[bar] = ability
            }
        }

        ball.concept.abilities.filter { it.progressBars.isNotEmpty() }.forEach { ability ->
            ability.progressBars.forEach { (pair, color) ->
                val amount = pair.first.get()
                val maxValue = pair.second
                val bar = ProgressBar().apply {
                    progress = amount / maxValue
                    prefWidth = INFO_WIDTH - 40.0
                    minWidth = prefWidth
                    maxWidth = prefWidth
                    prefHeight = 20.0
                    style = "-fx-accent: #${
                        String.format(
                            "%02X%02X%02X",
                            (color.red * 255).toInt(),
                            (color.green * 255).toInt(),
                            (color.blue * 255).toInt()
                        )
                    };"
                }
                pane.children.add(bar)
                additionalMap[bar] = pair.first to maxValue
            }
        }

        cooldownProgressBar[ball] = cooldownMap

        additionalProgressBar[ball] = additionalMap
    }

    private fun checkBalls() {
        if (match.balls.toList().any { it.health <= 0 }) {
            val winner = match.balls.toList().filter { it.health > 0 }
            if (winner.isNotEmpty()) {
                completeMatch(winner.single())
            } else {
                completeMatch(null)
            }
        }
    }

    private fun completeMatch(winner: Ball?) {
        resultLabel.isVisible = true
        backToMenu.isVisible = true

        resultLabel.text = when(winner) {
            null -> "무승부!"
            else -> "승자: ${winner.concept.displayName}!"
        }
        updateBallViews()

        stop()
    }

    private fun stop() {
        animationTimer.stop()
        match.stop()
    }
}
