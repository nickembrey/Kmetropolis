package engine.operation.state.player

import engine.card.Card
import engine.card.CardLocation
import engine.operation.PlayerOperation
import engine.operation.state.StateOperation
import util.memoize

data class PlayerMoveCardOperation(
    val card: Card,
    val from: CardLocation,
    val to: CardLocation,
): StateOperation, PlayerOperation {

    companion object {
        private fun getMove(card: Card, from: CardLocation, to: CardLocation): PlayerMoveCardOperation =
            PlayerMoveCardOperation(
                card = card,
                from = from,
                to = to
            )

        val MOVE_CARD: (Card, CardLocation, CardLocation) -> PlayerMoveCardOperation = ::getMove.memoize()
    }

    override val undo: StateOperation
        get() = PlayerMoveCardOperation(
            card = card,
            from = to,
            to = from
        )
}