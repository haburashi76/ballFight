package io.github.haburashi76.ballfight.loader

import io.github.haburashi76.ballfight.BallConcept
import io.github.haburashi76.ballfight.BallContainer
import io.github.haburashi76.ballfight.BallDescription
import java.io.File
import java.util.jar.JarFile

class BallLoader internal constructor() {
    private val classes = mutableMapOf<String, Class<*>>()

    private val classLoaders = mutableMapOf<File, BallClassLoader>()

    internal fun load(file: File, description: BallDescription): BallContainer {
        require(file !in classLoaders) { "Already registered file ${file.name}" }

        val classLoader = BallClassLoader(file, this, javaClass.classLoader)

        try {
            val conceptClass =
                Class.forName(description.main, true, classLoader).asSubclass(BallConcept::class.java)

            val abilityClasses =
                JarFile(file).use { jar ->
                    val path = (description.group +
                            ".ballfight" +
                            ".ball" +
                            ".${description.name.replace("-", "")}" +
                            ".ability."
                            ).replace(".", "/")
                    jar.entries()
                        .asSequence()
                        .filter { entry ->
                            entry.name.startsWith(path) &&
                                    entry.name.endsWith(".class")
                        }
                        .filter { entry ->
                            val identity = entry.name.removePrefix(path)
                            val parts = identity.split("/")

                            parts.single().startsWith("Ability")
                                    && !parts.single().contains("$")
                        }
                        .map { entry ->
                            val name = entry.name.removeSuffix(".class").replace("/", ".")
                            Class.forName(name, true, classLoader)
                        }.toList()
                }

            testCreateInstance(conceptClass)
            abilityClasses.forEach { testCreateInstance(it) }

            classLoaders[file] = classLoader

            return BallContainer(file, conceptClass, abilityClasses, description)
        } catch (e: Exception) {
            classLoader.close()
            throw e
        }
    }

    internal fun findClass(name: String, skip: BallClassLoader): Class<*> {
        var found = classes[name]

        if (found != null) return found

        for (loader in classLoaders.values) {
            if (loader === skip) continue

            try {
                found = loader.findLocalClass(name)
                classes[name] = found

                return found
            } catch (ignore: ClassNotFoundException) {}
        }

        throw ClassNotFoundException(name)
    }
    fun clear() {
        classes.clear()
        classLoaders.run {
            values.forEach(BallClassLoader::close)
            clear()
        }
    }
}

private fun testCreateInstance(clazz: Class<*>) {
    try {
        clazz.getConstructor().newInstance()
    } catch (e: Exception) {
        e.printStackTrace()
        error("Failed to create instance ${clazz.name}")
    }
}