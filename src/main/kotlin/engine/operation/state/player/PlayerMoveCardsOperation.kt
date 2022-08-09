package engine.operation.state.player

import engine.card.Card
import engine.card.CardLocation
import engine.operation.PlayerOperation
import engine.operation.state.StateOperation
import util.memoize

data class PlayerMoveCardsOperation(
    val cards: List<Card>,
    val from: CardLocation,
    val to: CardLocation,
): StateOperation, PlayerOperation {

    companion object {
        private fun getMove(cards: List<Card>, from: CardLocation, to: CardLocation): PlayerMoveCardsOperation =
            PlayerMoveCardsOperation(
                cards = cards,
                from = from,
                to = to
            )

        // TODO: memoize?
        val MOVE_CARDS: (List<Card>, CardLocation, CardLocation) -> PlayerMoveCardsOperation = ::getMove
    }

    override val undo: StateOperation
        get() = PlayerMoveCardsOperation(
            cards = cards,
            from = to,
            to = from
        )
}