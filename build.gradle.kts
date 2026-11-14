import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.8.22" apply false
    id("org.openjfx.javafxplugin") version "0.0.13" apply false
    id("org.javamodularity.moduleplugin") version "1.8.12" apply false
    id("org.beryx.jlink") version "2.25.0" apply false
}

allprojects {
    repositories {
        mavenCentral()
    }
    tasks {
        withType<KotlinCompile> {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")

    apply(plugin = "org.openjfx.javafxplugin")
    apply(plugin = "org.javamodularity.moduleplugin")
}