package util

import engine.operation.Operation
import java.util.*

/*
* Copyright (c) 2011.  Peter Lawrey
*
* "THE BEER-WARE LICENSE" (Revision 128)
* As long as you retain this notice you can do whatever you want with this stuff.
* If we meet some day, and you think this stuff is worth it, you can buy me a beer in return
* There is no warranty.
*/

class SimpleIntHashMap {
    private var keys = IntArray(16)

    init {
        Arrays.fill(keys, NO_VALUE)
    }

    private var values = arrayOfNulls<Operation?>(16)
    private var lastKey = keys.size / 2 - 1

    operator fun get(key: Int): Operation? {
        val hash = key and lastKey
        return if(hash > (keys.size - 1)) null else values[hash]
    }

    fun put(key: Int, value: Operation) {
        var hash = key and lastKey
        if (keys[hash] != NO_VALUE && keys[hash] != key) {
            resize()
            hash = key and lastKey
            if (keys[hash] != NO_VALUE && keys[hash] != key) throw UnsupportedOperationException("Unable to handle collision.")
        }
        keys[hash] = key
        values[hash] = value
    }

    private fun resize() {
        val len2 = keys.size * 2
        val keys2 = IntArray(len2)
        Arrays.fill(keys2, NO_VALUE)
        val values2 = arrayOfNulls<Operation?>(len2)
        lastKey = len2 - 1
        for (i in keys.indices) {
            val key = keys[i]
            val value = values[i]
            if (key == NO_VALUE) continue
            val hash = key and lastKey
            if (keys2[hash] != NO_VALUE) throw UnsupportedOperationException("Unable to handle collision.")
            keys2[hash] = key
            values2[hash] = value
        }
        keys = keys2
        values = values2
    }

    fun clear() {
        Arrays.fill(keys, NO_VALUE)
    }

    companion object {
        const val NO_VALUE = Int.MIN_VALUE
    }
}