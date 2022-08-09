package engine.operation.state.game

import engine.branch.BranchContext
import engine.card.Card
import engine.operation.GameOperation
import engine.operation.HistoryOperation
import engine.operation.PlayerOperation
import engine.operation.stack.StackOperation
import engine.operation.stack.player.PlayerCardOperationType
import engine.operation.state.StateOperation

data class GameCardOperation(
    val type: GameCardOperationType,
    val card: Card
): StateOperation, GameOperation, HistoryOperation {

    override val undo
        get() = when(this.type) {
        GameCardOperationType.INCREMENT_CARD_SUPPLY -> GameCardOperation(
            type = GameCardOperationType.DECREMENT_CARD_SUPPLY,
            card = card
        )
        GameCardOperationType.DECREMENT_CARD_SUPPLY -> GameCardOperation(
            type = GameCardOperationType.INCREMENT_CARD_SUPPLY,
            card = card
        )
    }

    companion object {
        // TODO: check performance on lambda vs regular fn
        // TODO: use these
        val INCREMENT_SUPPLY: (Card) -> GameCardOperation = {
            GameCardOperation(
                type = GameCardOperationType.INCREMENT_CARD_SUPPLY,
                card = it
            )
        }

        val DECREMENT_SUPPLY: (Card) -> GameCardOperation = {
            GameCardOperation(
                type = GameCardOperationType.DECREMENT_CARD_SUPPLY,
                card = it
            )
        }

    }
}