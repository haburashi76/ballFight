package io.github.haburashi76.ballfight.view

import io.github.haburashi76.ballfight.BallFightApplication
import io.github.haburashi76.ballfight.buttonFont
import io.github.haburashi76.ballfight.titleFont
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.BorderPane
import javafx.scene.layout.VBox
import javafx.stage.Stage

class MainMenu(
    private val app: BallFightApplication,
    private val stage: Stage
): BorderPane() {
    init {

        val title = Label("BallFight")
        title.font = titleFont

        val titleBox = VBox(title)

        titleBox.alignment = Pos.CENTER
        titleBox.padding = Insets(50.0)

        top = titleBox

        val reload = Button("파일 다시 불러오기")

        reload.font = buttonFont

        reload.isFocusTraversable = false

        reload.setOnAction {
            app.reload()
        }

        val start = Button("시작하기")

        start.isFocusTraversable = false

        start.font = buttonFont

        start.setOnAction {
            stage.scene =
                Scene(
                    BallSelectMenu(app, stage),
                    960.0, 720.0
                )
        }

        val menu = VBox(
            128.0,
            start,
            reload
        )
        menu.alignment = Pos.CENTER

        center = menu
    }
}