
subprojects {
    javafx {
        version = "17"
        modules(
            "javafx.controls",
            "javafx.fxml",
            "javafx.media"
        )
    }

    if (version == "unspecified") version = rootProject.version

    dependencies {
        compileOnly(project(":ballFight-core"))
    }

    val projectName = name.removePrefix("ball-")

    project.extra.apply {
        set("projectName", projectName)
        set("packageName", projectName.replace("-", ""))
        @Suppress("Deprecation")
        set("ballName", projectName.split('-').joinToString(separator = "") { it.capitalize() })
    }

    tasks {
        processResources {
            filesMatching("**/*.yml") {
                expand(project.properties)
            }
        }

        val ballFightJar = register<Jar>("ballFightJar") {
            archiveVersion = ""
            archiveBaseName = "${project.group}.$projectName"
            from(sourceSets["main"].output)
        }
        rootProject.tasks {
            register<DefaultTask>(projectName) {
                dependsOn(ballFightJar)
            }
        }
    }
}
