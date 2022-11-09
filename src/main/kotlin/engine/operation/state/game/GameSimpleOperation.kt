package engine.operation.state.game

import engine.operation.GameOperation
import engine.operation.HistoryOperation
import engine.operation.state.StateOperation

// TODO: put check game over somewhere
// TODO: rethink how handling game over

enum class GameSimpleOperation(val verb: String): GameOperation, StateOperation, HistoryOperation {

    INCREMENT_TURNS("Turn ends"), DECREMENT_TURNS("Undo turn end"),
    INCREMENT_EMPTY_PILES("A pile runs out"), DECREMENT_EMPTY_PILES("An empty pile has a card again"),
    SWITCH_PLAYER("Current player switches"),
    END_GAME("The game ends"), UNDO_END_GAME("The game restarts");

    override val undo: StateOperation
        get() = when(this) {
            INCREMENT_TURNS -> DECREMENT_TURNS
            DECREMENT_TURNS -> INCREMENT_TURNS
            INCREMENT_EMPTY_PILES -> DECREMENT_EMPTY_PILES
            DECREMENT_EMPTY_PILES -> INCREMENT_EMPTY_PILES
            SWITCH_PLAYER -> SWITCH_PLAYER
            END_GAME -> UNDO_END_GAME
            UNDO_END_GAME -> END_GAME
        }
}