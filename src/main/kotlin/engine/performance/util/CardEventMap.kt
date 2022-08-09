package engine.performance.util

import engine.CardMap
import engine.GameEvent
import engine.card.Card
import engine.operation.Operation.Companion.NO_OP

open class CardEventMap(private val initialValues: Map<Card, GameEvent>): CardMap<GameEvent> {

    private val maxOrdinal: Int = initialValues.keys.maxOf { card -> card.ordinal }

    protected val backingArray: Array<GameEvent> = Array(maxOrdinal + 1) { index ->
        initialValues.entries.firstOrNull { it.key.ordinal == index }?.value ?: NO_OP
    }

    override operator fun get(card: Card): GameEvent = backingArray[card.ordinal]

    override operator fun set(card: Card, value: GameEvent) {
        backingArray[card.ordinal] = value
    }

}