package engine.operation.property

import engine.GamePhase
import engine.GameProperty
import engine.card.Card
import engine.operation.HistoryOperation
import engine.operation.state.StateOperation
import engine.player.PlayerProperty
import util.memoize
import util.memoizeEnum
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

        val SET_REMODEL_CARD: (Card?, Card?) -> SetFromPropertyOperation<Card?> =
            ::setRemodelCard.memoize()

        @Suppress("UNCHECKED_CAST")
        val NEXT_PHASE: (GamePhase) -> SetFromPropertyOperation<GamePhase> = ::nextPhase
            .memoizeEnum(SetFromPropertyOperation::class.java, GamePhase.values().size)
                as (GamePhase) -> SetFromPropertyOperation<GamePhase>
    }
}