package engine.operation.state.player

import engine.operation.HistoryOperation
import engine.operation.Operation
import engine.operation.PlayerOperation
import engine.operation.state.StateOperation

//  // TODO: shuffle needs to trigger a move of each individual card

enum class PlayerSimpleOperation(val verb: String): StateOperation, PlayerOperation, HistoryOperation {
    INCREMENT_BUYS("adds a buy"), DECREMENT_BUYS("removes a buy"),
    INCREMENT_COINS("adds a coin"), DECREMENT_COINS("removes a coin"),
    INCREMENT_VP("adds a victory point"), DECREMENT_VP("removes a victory point");

    override val undo: StateOperation
        get() = when(this) {
            INCREMENT_BUYS -> DECREMENT_BUYS
            DECREMENT_BUYS -> INCREMENT_BUYS
            INCREMENT_COINS -> DECREMENT_COINS
            DECREMENT_COINS -> INCREMENT_COINS
            INCREMENT_VP -> DECREMENT_VP
            DECREMENT_VP -> INCREMENT_VP
        }
}