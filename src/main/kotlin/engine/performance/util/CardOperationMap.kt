package engine.performance.util

import engine.CardMap
import engine.GameEvent
import engine.card.Card
import engine.operation.Operation
import engine.operation.Operation.Companion.NO_OP
import engine.operation.stack.StackMultipleOperation

open class CardOperationMap(private val initialValues: Map<Card, Operation>): CardMap<Operation> {

    private val maxOrdinal: Int = initialValues.keys.maxOf { card -> card.ordinal }

    protected val backingArray: Array<Operation> = Array(maxOrdinal + 1) { index ->
        initialValues.entries.firstOrNull { it.key.ordinal == index }?.value ?: NO_OP
    }

    override operator fun get(card: Card): Operation = backingArray[card.ordinal]

    override operator fun set(card: Card, value: Operation) {
        backingArray[card.ordinal] = value
    }

}