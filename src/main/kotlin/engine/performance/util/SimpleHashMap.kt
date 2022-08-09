package engine.performance.util

import engine.GameEvent
import engine.card.Card

class SimpleHashMap <T> (arrayClass: Class<T>, maxSize: Int) {

    @Suppress("UNCHECKED_CAST")
    private val backingArray: Array<T?> = java.lang.reflect.Array.newInstance(arrayClass, maxSize) as Array<T?>

    operator fun get(key: Int): T? = backingArray[key]

    operator fun set(key: Int, value: T) {
        backingArray[key] = value
    }
}