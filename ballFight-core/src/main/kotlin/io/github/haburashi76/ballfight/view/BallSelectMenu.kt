package io.github.haburashi76.ballfight.view

import io.github.haburashi76.ballfight.*
import io.github.haburashi76.ballfight.ability.Ability
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.Border
import javafx.scene.layout.BorderStroke
import javafx.scene.layout.BorderStrokeStyle
import javafx.scene.layout.BorderWidths
import javafx.scene.layout.CornerRadii
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.stage.Stage

class BallSelectMenu(
    private val app: BallFightApplication,
    private val stage: Stage
): VBox() {
    private var selected1: String? = null
    private var selected2: String? = null

    init {
        alignment = Pos.CENTER
        spacing = 20.0

        val select1Group = ToggleGroup()
        val select2Group = ToggleGroup()

        val select1Box = optionsList(select1Group) {
            selected1 = if (selected1 == it) null else it
        }
        val select2Box = optionsList(select2Group) {
            selected2 = if (selected2 == it) null else it
        }

        val start = Button("시작하기")

        start.isFocusTraversable = false
        start.font = buttonFont
        start.setOnAction {
            val first = app.ballContainers[selected1]
                ?: return@setOnAction

            val second = app.ballContainers[selected2]
                ?: return@setOnAction


            val match = Match(createBall(first) to createBall(second))

            stage.scene =
                Scene(
                    BattleScene(match, app, stage),
                    960.0,
                    720.0
                )
        }
        children.addAll(
            Label("Ball 1").apply {
                font = labelFont
            },
            select1Box,

            Label("Ball 2").apply {
                font = labelFont
            },
            select2Box,

            start
        )
    }
    private fun optionsList(
        group: ToggleGroup,
        onSelect: (String) -> Unit
    ): ScrollPane {

        val box = VBox(5.0)

        app.ballContainers.forEach { (id, container) ->
            val button = ToggleButton(
                (container.ballConceptClass
                    .getConstructor()
                    .newInstance() as BallConcept)
                    .displayName + "  by. ${container.ballDescription.author}"
            )

            button.toggleGroup = group

            button.font = optionFont

            button.isFocusTraversable = false

            button.setOnAction {
                onSelect(id)
            }

            box.children.add(button)
        }

        val pane = ScrollPane(box)

        pane.border = Border(
            BorderStroke(
                Color.GRAY,
                BorderStrokeStyle.SOLID,
                CornerRadii.EMPTY,
                BorderWidths(2.0)
            )
        )

        pane.maxWidth = 760.0
        pane.prefHeight = 180.0

        return pane
    }
    private fun createBall(container: BallContainer): Ball {
        val concept = container
            .ballConceptClass
            .getConstructor()
            .newInstance() as BallConcept

        val instance = concept.createBallInstance()

        instance.file = container.file

        val abilities = container.abilityClasses.map { clazz ->
            clazz.getConstructor().newInstance() as Ability
        }

        abilities.forEach { it.initialize(concept, instance) }

        concept.abilities = abilities

        concept.icon = container.file.getIcon()

        return instance
    }
}