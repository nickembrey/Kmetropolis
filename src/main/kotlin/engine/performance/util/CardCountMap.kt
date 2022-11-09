package engine.performance.util

import com.github.shiguruikai.combinatoricskt.Combinatorics
import com.marcinmoskala.math.factorial
import engine.CardMap
import engine.card.Card

// TODO: change initialValues to Board
class CardCountMap(
    private val board: Map<Card, Int>,
    private val initialValues: Map<Card, Int>): CardMap<Int> {

    companion object {

        fun fromKnownDeck(board: CardCountMap, knownDeck: Map<Int, Card>): CardCountMap {
            val map = mutableMapOf<Card, Int>()
            knownDeck.values.forEach {
                map.merge(it, 1, Int::plus)
            }
            return CardCountMap(board.toMap(), map)
        }

        fun empty(board: CardCountMap): CardCountMap {
            return CardCountMap(board.toMap(), board.toMap().keys.associateWith { 0 })
        }
    }

    private val cardsByOrdinal: Map<Int, Card> = board.keys.associateBy { it.ordinal }
    private val maxOrdinal: Int = board.keys.maxOf { card -> card.ordinal }

    private val backingArray: IntArray = IntArray(maxOrdinal + 1) { index ->
        initialValues.keys.firstOrNull { it.ordinal == index }.let { initialValues[it] } ?: 0
    }

    fun clear() {
        backingArray.indices.forEach {
            backingArray[it] = 0
        }
    }

    operator fun plus(other: CardCountMap): CardCountMap {
        return CardCountMap(board, board.keys.associate {
            (it to this[it] + other[it])
        })
    }

    fun add(other: CardCountMap) {
        board.keys.forEach {
            this[it] += other[it]
        }
    }

    fun copy(): CardCountMap {
        return CardCountMap(board, this.toMap())
    }

    fun getCombinations(choose: Int, set: Set<Int> = possibleOrdinals): Map<List<Card>, Double> {

        val cardValues = Card.values()

        return Combinatorics.combinationsWithRepetition(set, choose)
            .associateWith { list ->
            var combinationProbability: Double = list.size.factorial() / list.groupBy { it }.values.fold(1L) {
                acc, ints -> acc * ints.size.factorial()
            }.toDouble()
            for (i in list.indices) {
                val previousOrdinals = list.subList(0, i)
                val cardProbability = getProbability(list[i], previousOrdinals)
                combinationProbability *= cardProbability
            }
            combinationProbability
        }.filter { it.value > 0 }.mapKeys { it.key.map { num -> cardValues[num] } }
    }

    private val possibleOrdinals: Set<Int>
        get() = backingArray.foldIndexed(mutableSetOf()) { i, r, c ->
            if (c > 0) r.apply { add(i) } else r
        }

    val possibilities: MutableList<Card> // TODO: make this not a getter
        get() = board.keys.filter { backingArray[it.ordinal] > 0 }.toMutableList()

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

    operator fun compareTo(freqMap: Map<Card, Int>): Int {
        var ret = 0
        for(entry in freqMap.entries) {
            if(entry.value > this[entry.key]) {
                return -1
            } else if(entry.value < this[entry.key]) {
                ret = 1
            }
        }
        return ret
    }

    operator fun compareTo(cards: List<Card>): Int {
        return compareTo(cards.groupingBy { it }.eachCount())
    }

    fun random(): Card {
        var random = kotlin.random.Random.nextInt(0, size)
        for(index in backingArray.indices) {
            random -= backingArray[index]
            if(random < 0) {
                return cardsByOrdinal[index]!!
            }
        }
        throw IllegalStateException() // TODO: clean this up
    }

    private fun getProbability(ordinal: Int, minus: List<Int>): Double {
        return (backingArray[ordinal].toDouble() - minus.count { it == ordinal }) / (backingArray.sum().toDouble() - minus.size)
    }

    val probabilities: Map<Card, Double>
        get() = possibilities
            .associateWith { card -> backingArray[card.ordinal].toDouble() / size.toDouble() }

    fun toList(): List<Card> = possibilities
        .flatMap { card -> List(backingArray[card.ordinal]) { card } }

    fun toMap(): Map<Card, Int> = board.keys
        .associateWith { card -> backingArray[card.ordinal] }

}