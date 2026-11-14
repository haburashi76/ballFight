plugins {
    application
    id("org.beryx.jlink")
}

javafx {
    version = "17"
    modules(
        "javafx.controls",
        "javafx.fxml",
        "javafx.media"
    )
}

application {
    mainClass.set(
        "io.github.haburashi76.ballfight.BallFightApplicationKt"
    )
}

jlink {
    options.set(
        listOf(
            "--strip-debug",
            "--compress",
            "2",
            "--no-header-files",
            "--no-man-pages"
        )
    )

    launcher {
        name = "ball-fight"
    }
}