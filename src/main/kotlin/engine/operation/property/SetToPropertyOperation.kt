package engine.operation.property

import engine.GameProperty
import engine.branch.BranchContext
import engine.card.Card
import engine.operation.state.StateOperation
import engine.player.PlayerProperty
import util.memoize
import util.memoizeEnum

data class SetToPropertyOperation<T>(
    val target: StateProperty, // TODO: numeric and non numeric state properties
    val to: T,
): PropertyOperation {
    companion object {
        private fun setBuys(to: Int) =
            SetToPropertyOperation(
                to = to,
                target = PlayerProperty.BUYS)
        private fun setCoins(to: Int) =
            SetToPropertyOperation(
                to = to,
                target = PlayerProperty.COINS)
        private fun setRemodelCard(to: Card) =
            SetToPropertyOperation(
                to = to,
                target = PlayerProperty.REMODEL_CARD
            )

        val SET_BUYS: (Int) -> SetToPropertyOperation<Int> = ::setBuys.memoize()
        val SET_COINS: (Int) -> SetToPropertyOperation<Int> = ::setCoins.memoize()
        val SET_REMODEL_CARD: (Card) -> SetToPropertyOperation<Card> = ::setRemodelCard.memoize()
    }
}