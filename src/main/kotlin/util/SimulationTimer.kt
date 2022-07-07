package util

import kotlin.properties.Delegates

class SimulationTimer {

    var started = false

    private var start: Long by Delegates.notNull()
    private var stop: Long by Delegates.notNull()
    private val splits: MutableList<Long> = mutableListOf()

    val averageInterval
        get() = splits.mapIndexed { index, it ->
            when(index) {
                0 -> it - start
                else -> it - splits[index - 1]
            }
        }.average()

    val time
        get() = stop - start

    var totalTime: Long = 0

    fun start(): SimulationTimer {
        start = System.currentTimeMillis()
        splits.clear()
        started = true
        return this
    }

    fun split() {
        System.currentTimeMillis().let {
            splits.add(it - start)
        }
    }

    fun stop() {
        stop = System.currentTimeMillis()
        totalTime += time
    }

}