package engine.performance.util

import engine.CardMap
import engine.card.Card

// TODO: change initialValues to Board
class CardCountMap(
    private val board: Map<Card, Int>,
    private val initialValues: Map<Card, Int>): CardMap<Int> {

    private val cardsByOrdinal: Map<Int, Card> = board.keys.associateBy { it.ordinal }
    private val maxOrdinal: Int = board.keys.maxOf { card -> card.ordinal }

    private val backingArray: IntArray = IntArray(maxOrdinal + 1) { index ->
        initialValues.keys.firstOrNull { it.ordinal == index }.let { initialValues[it] } ?: 0
    }

    val possibilities: MutableSet<Card> = board.keys
        .filter { backingArray[it.ordinal] > 0 }.toMutableSet()

    val size: Int
        get() = backingArray.sum()

    override operator fun get(card: Card): Int = backingArray[card.ordinal]

    override operator fun set(card: Card, value: Int) {
        when(value) {
            0 -> possibilities.remove(card)
            in 1..Int.MAX_VALUE -> possibilities.add(card)
            else -> throw IllegalStateException()
        }
        backingArray[card.ordinal] = value
    }

    fun random(): Card {
        var random = kotlin.random.Random.nextInt(0, size)
        for(index in backingArray.indices) {
            random -= backingArray[index]
            if(random < 0) {
                return cardsByOrdinal[index]!!
            }
        }
        throw java.lang.IllegalStateException() // TODO: clean this up
    }

    val probabilities: Map<Card, Double>
        get() = possibilities
            .associateWith { card -> backingArray[card.ordinal].toDouble() / size.toDouble() }

    fun toList(): List<Card> = possibilities
        .flatMap { card -> List(backingArray[card.ordinal]) { card } }

    fun toMap(): Map<Card, Int> = board.keys
        .associateWith { card -> backingArray[card.ordinal] }

}