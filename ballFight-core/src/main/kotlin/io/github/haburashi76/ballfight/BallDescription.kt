package io.github.haburashi76.ballfight

import java.io.BufferedReader

class BallDescription(private val reader: BufferedReader) {
    private fun read(): Map<String, String> {
        val map = mutableMapOf<String, String>()
        reader.readLines().forEach { line ->
            val parts = line.split(": ", limit = 2)
            if (parts.size == 2) {
                val key = parts[0].trim()
                val value = parts[1].trim()
                map[key] = value
            }
        }
        return map
    }
    private val read = read()

    val group = requireNotNull(read["group"]) { "group is not defined" }
    val name = requireNotNull(read["name"]) { "name is not defined" }
    val version = requireNotNull(read["version"]) { "version is not defined" }
    val main = requireNotNull(read["main"]) { "main is not defined" }
    val author = read["author"]
    val artifactId = "$group.$name"
}