package io.github.haburashi76.ballfight

import io.github.haburashi76.ballfight.loader.BallLoader
import io.github.haburashi76.ballfight.view.MainMenu
import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.scene.text.Font
import javafx.stage.Stage
import java.io.File
import java.util.jar.JarFile

class BallFightApplication : Application() {

    private val loader = BallLoader()

    lateinit var ballContainers: Map<String, BallContainer>
        private set

    private val ballFolder = File(
        File(
            this::class
                .java
                .protectionDomain
                .codeSource
                .location
                .toURI()
        ).parentFile,
        "balls"
    )
    private val updateFolder = File(ballFolder, "update")

    override fun start(stage: Stage) {

        updateFiles()

        println(ballFolder.toURI().toURL().toString())

        loadBalls()

        val scene = Scene(MainMenu(this, stage), 960.0, 720.0)
        stage.isResizable = false
        stage.title = "BallFight"
        stage.scene = scene
        stage.icons.add(
            Image(javaClass.getResourceAsStream("/icon.png"))
        )
        stage.show()
    }

    private fun getUpdateFiles(): Array<File> {
        updateFolder.mkdirs()
        return updateFolder.listFiles { file -> !file.isDirectory && file.name.endsWith(".jar") }?: return emptyArray()
    }

    private fun getBallFiles(): Array<File> {
        ballFolder.mkdirs()
        return ballFolder.listFiles { file -> !file.isDirectory && file.name.endsWith(".jar") }?: return emptyArray()
    }

    fun reload() {
        loader.clear()
        updateFiles()
        loadBalls()
    }

    private fun updateFiles() {
        val ballFiles = getBallFiles()
        if (ballFiles.isEmpty()) return

        val updateFolder = File(ballFolder, "update")
        val updated = arrayListOf<File>()

        for (ballFile in ballFiles) {
            val updateFile = File(updateFolder, ballFile.name)

            if (updateFile.exists()) {
                updateFile.runCatching {
                    copyTo(ballFile, true)
                }.onSuccess {
                    updated += it
                    updateFile.runCatching { delete() }
                }.onFailure {
                    it.printStackTrace()
                    println("Failed to update ability ${updateFile.nameWithoutExtension}")
                }
            }
        }

        println("Updated abilities(${updated.count()}): ")

        updated.forEach { file ->
            println("  - ${file.nameWithoutExtension}")
        }
    }

    private fun loadBalls() {
        val descriptions = loadDescriptions()
        val map = mutableMapOf<String, BallContainer>()

        descriptions.forEach { (file, description) ->
            loader.runCatching {
                map[description.artifactId] = load(file, description)
            }.onFailure { e: Throwable ->
                e.printStackTrace()
                error("Failed to load Ability ${file.name}")
            }
        }
        println("Loaded balls(${map.count()}):")

        map.forEach { (id, container) ->
            println("   - $id v${container.ballDescription.version}")
        }

        ballContainers = map
    }

    private fun loadDescriptions(): List<Pair<File, BallDescription>> {
        val files = getBallFiles()

        val map = mutableMapOf<String, Pair<File, BallDescription>>()

        files.forEach { file ->
            file.runCatching { getBallDescription() }
                .onSuccess { description ->
                    val id = description.artifactId
                    map[id] = file to description
                }
                .onFailure { e ->
                    e.printStackTrace()
                    error("Failed to load BallDescription ${file.name}")
                }
        }
        return map.values.toList()
    }
}

fun main() {
    Application.launch(BallFightApplication::class.java)
}

private fun File.getBallDescription(): BallDescription {
    JarFile(this).use { jar ->
        jar.getJarEntry("ball.yml")?.let { entry ->
            jar.getInputStream(entry).bufferedReader().use { reader ->
                return BallDescription(reader)
            }
        }
    }
    error("Failed to open JarFile $name")
}

fun File.getIcon(): Image {
    JarFile(this).use { jar ->
        jar.getJarEntry("icon.png")?.let { entry ->
            jar.getInputStream(entry).use {
                return Image(it)
            }
        }
    }

    error("Failed to get Icon")
}

val titleFont = Font("Pretendard", 128.0)
val buttonFont = Font("Noto Sans KR", 64.0)
val labelFont = Font("Pretendard", 48.0)
val optionFont = Font("Pretendard", 22.0)
val smallLabelFont = Font("Pretendard", 45.0)