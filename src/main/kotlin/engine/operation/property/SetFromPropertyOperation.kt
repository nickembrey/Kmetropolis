package engine.operation.property

import engine.GamePhase
import engine.GameProperty
import engine.branch.BranchContext
import engine.card.Card
import engine.operation.HistoryOperation
import engine.operation.state.StateOperation
import engine.player.PlayerProperty
import util.memoize
import util.memoizeEnum

// TODO: better name?
class SetFromPropertyOperation<T>(
    val target: StateProperty,
    val from: T,
    val to: T,
): StateOperation, PropertyOperation, HistoryOperation {
    override val undo: StateOperation
        get() = SetFromPropertyOperation(
            from = to,
            to = from,
            target = target
        )

    companion object {

        private fun setBuys(from: Int, to: Int) =
            SetFromPropertyOperation(
                from = from,
                to = to,
                target = PlayerProperty.BUYS)
        private fun setCoins(from: Int, to: Int) =
            SetFromPropertyOperation(
                from = from,
                to = to,
                target = PlayerProperty.COINS)
        private fun setVp(from: Int, to: Int) =
            SetFromPropertyOperation(
                from = from,
                to = to,
                target = PlayerProperty.BASE_VP)
        private fun nextPhase(currentPhase: GamePhase) =
            SetFromPropertyOperation(
                from = currentPhase,
                to = currentPhase.next,
                target = GameProperty.PHASE
            )
        private fun setRemodelCard(from: Card?, to: Card?) =
            SetFromPropertyOperation(
                from = from,
                to = to,
                target = PlayerProperty.REMODEL_CARD)

        val SET_BUYS: (Int, Int) -> SetFromPropertyOperation<Int> = ::setBuys.memoize()
        val SET_COINS: (Int, Int) -> SetFromPropertyOperation<Int> = ::setCoins.memoize()
        val SET_VP: (Int, Int) -> SetFromPropertyOperation<Int> = ::setVp.memoize()
        val SET_REMODEL_CARD: (Card?, Card?) -> SetFromPropertyOperation<Card?> =
            ::setRemodelCard.memoize()

        @Suppress("UNCHECKED_CAST")
        val NEXT_PHASE: (GamePhase) -> SetFromPropertyOperation<GamePhase> = ::nextPhase
            .memoizeEnum(SetFromPropertyOperation::class.java, GamePhase.values().size)
                as (GamePhase) -> SetFromPropertyOperation<GamePhase>
    }
}