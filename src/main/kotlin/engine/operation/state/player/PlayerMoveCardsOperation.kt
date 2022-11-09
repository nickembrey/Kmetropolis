package engine.operation.state.player

import engine.card.Card
import engine.card.CardLocation
import engine.operation.PlayerOperation
import engine.operation.state.StateOperation

data class PlayerMoveCardsOperation(
    val cards: List<Card>,
    val from: CardLocation,
    val to: CardLocation,
): StateOperation, PlayerOperation {

    override val undo: StateOperation
        get() = PlayerMoveCardsOperation(
            cards = cards,
            from = to,
            to = from
        )
}