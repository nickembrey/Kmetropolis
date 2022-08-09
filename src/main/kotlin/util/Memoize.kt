package util

import engine.card.Card
import engine.card.CardLocation
import engine.operation.state.player.PlayerMoveCardOperation
import engine.performance.util.SimpleHashMap
import kotlin.collections.HashMap

// https://www.raywenderlich.com/books/functional-programming-in-kotlin-by-tutorials/v1.0/chapters/4-expression-evaluation-laziness-more-about-functions
fun <A, B> ((A) -> B).memoize(): (A) -> B {

    val cache by lazy { HashMap<A, B>() } // 2
    return { a: A ->  // 3
        val cached = cache[a] // 4
        if (cached == null) {
            cache[a] = this(a)
            this(a)
        } else {
            cached
        }
    }
}

fun <A: Enum<A>, B> ((A) -> B).memoizeEnum(opClass: Class<B>, maxSize: Int): (A) -> B {
    val cache = SimpleHashMap(opClass, maxSize)
    return { a: A ->
        val cached = cache[a.ordinal]
        if (cached == null) {
            val new = this(a)
            cache[a.ordinal] = new
            new
        } else {
            cached
        }
    }
}

fun <A, B> ((A, A) -> B).memoize(): (A, A) -> B { // 1
    val cache by lazy { HashMap<Pair<A, A>, B>() } // 2
    return { a1: A, a2: A ->  // 3
        val pair = Pair(a1, a2)
        val cached = cache[pair] // 4
        if (cached == null) {
            cache[pair] = this(a1, a2)
            this(a1, a2)
        } else {
            cached
        } // 5
    }
}

// TODO: better name
fun <A, B, C> ((A, B) -> C).memoize2(): (A, B) -> C { // 1
    val cache by lazy { HashMap<Pair<A, B>, C>() } // 2
    return { a1: A, a2: B ->  // 3
        val pair = Pair(a1, a2)
        val cached = cache[pair] // 4
        if (cached == null) {
            cache[pair] = this(a1, a2)
            this(a1, a2)
        } else {
            cached
        } // 5
    }
}

fun ((Card, CardLocation, CardLocation) -> PlayerMoveCardOperation).memoize():
            (Card, CardLocation, CardLocation) -> PlayerMoveCardOperation
{
    val cards = Card.values().size
    val cardLocations = CardLocation.values().size
    val maxSize = (cards * cards + cardLocations) * cardLocations + cardLocations
    val cache = SimpleHashMap(PlayerMoveCardOperation::class.java, maxSize)

    return { a1: Card, a2: CardLocation, a3: CardLocation ->  // 3
        // https://stackoverflow.com/a/1362712/5374021
        val hash = (a1.ordinal * cards + a2.ordinal) * cardLocations + a3.ordinal
        val cached = cache[hash] // 4
        if (cached == null) {
            cache[hash] = this(a1, a2, a3)
            this(a1, a2, a3)
        } else {
            cached
        }
    }
}
