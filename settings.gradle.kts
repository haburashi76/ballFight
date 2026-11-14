rootProject.name = "ballFight"
include(rootProject.name + "-core")
include(rootProject.name + "-ball")

file(rootProject.name + "-ball").listFiles()?.filter { it.isDirectory && it.name.startsWith("ball-") }?.forEach {
    include(":ballFight-ball:${it.name}")
}