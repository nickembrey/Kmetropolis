package engine.operation.stack.player

import engine.branch.BranchContext
import engine.card.Card
import engine.operation.PlayerOperation
import engine.operation.stack.StackOperation

data class PlayerCardOperation(
    val type: PlayerCardOperationType,
    val card: Card,
    override val context: BranchContext
): StackOperation, PlayerOperation {
    companion object {
        // TODO: check performance on lambda vs regular fn
        val DRAW: (Card) -> PlayerCardOperation = {
            PlayerCardOperation(
                type = PlayerCardOperationType.DRAW,
                card = it,
                context = BranchContext.NONE
            )
        }
        val BUY: (Card) -> PlayerCardOperation = {
            PlayerCardOperation(
                type = PlayerCardOperationType.BUY,
                card = it,
                context = BranchContext.NONE
            )
        }
        val GAIN: (Card) -> PlayerCardOperation = {
            PlayerCardOperation(
                type = PlayerCardOperationType.GAIN,
                card = it,
                context = BranchContext.NONE
            )
        }
        val PLAY: (Card) -> PlayerCardOperation = {
            PlayerCardOperation(
                type = PlayerCardOperationType.PLAY,
                card = it,
                context = BranchContext.NONE
            )
        }
        val TRASH: (Card) -> PlayerCardOperation = {
            PlayerCardOperation(
                type = PlayerCardOperationType.TRASH,
                card = it,
                context = BranchContext.NONE
            )
        }
    }
}