package io.github.haburashi76.ballfight.tick

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

object TickManager {
    val repeatingTasks = ConcurrentHashMap<UUID, Pair<Runnable, Triple<Long, Long, Long>>>()
    val oneOffTasks = ConcurrentHashMap<Runnable, Pair<Long, Long>>()
    var currentTicks = 0L

    fun update() {
        repeatingTasks.forEach { _, (task, triple) ->
            val elapsed = currentTicks - (triple.first+triple.second)
            if (elapsed >= 0L && elapsed % triple.third == 0L) {
                task.run()
            }
        }
        val remove = mutableListOf<Runnable>()
        oneOffTasks.forEach { task, (start, delay) ->
            if (currentTicks > start+delay) remove.add(task) //혹시 모를 버그 대비
            if (currentTicks == start+delay) {
                task.run()
                remove.add(task)
            }
        }
        remove.forEach { oneOffTasks.remove(it) }
        currentTicks++
    }

    fun runTaskTimer(runnable: Runnable, delay: Long, period: Long): UUID {
        if (period <= 0) error("Period Must Be Positive Number")
        if (delay < 0) error("Delay Must Be Positive Number (or Zero)")
        val id = UUID.randomUUID()
        repeatingTasks[id] = runnable to Triple(currentTicks, delay, period)
        return id
    }

    fun runTask(runnable: Runnable, delay: Long) {
        if (delay < 0) error("Delay Must Be Positive Number (or Zero)")
        oneOffTasks[runnable] = currentTicks to delay
    }

    fun unRegister(id: UUID) {
        repeatingTasks.remove(id)
    }
}