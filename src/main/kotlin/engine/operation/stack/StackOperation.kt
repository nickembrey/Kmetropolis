package engine.operation.stack

import engine.ContextBearer
import engine.GameState
import engine.branch.Branch
import engine.branch.BranchContext
import engine.card.Card
import engine.card.CardLocation
import engine.operation.Operation
import engine.operation.property.ReadPropertyOperation
import engine.operation.property.SetToPropertyOperation
import engine.operation.state.game.GameSimpleOperation
import engine.operation.state.player.PlayerMoveCardOperation
import engine.operation.state.player.PlayerMoveCardsOperation
import engine.player.PlayerNumber
import util.memoize
import util.memoize2

interface StackOperation: Operation, ContextBearer {
    companion object {

        private fun opponentHandRedraw(
            currentPlayerNumber: PlayerNumber,
            otherPlayerHand: List<Card>
        ): StackOperation {
            return StackMultipleOperation(
                events = listOf(
                    StackConditionalOperation(
                        condition = { it.currentPlayerNumber == currentPlayerNumber },
                        conditionalEvent = GameSimpleOperation.SWITCH_PLAYER,
                        context = BranchContext.NONE
                    ),
                    PlayerMoveCardsOperation(
                        cards = otherPlayerHand,
                        from = CardLocation.HAND,
                        to = CardLocation.DECK
                    ),
                    Branch(BranchContext.DRAW, selections = otherPlayerHand.size)
                ).plus(
                    StackConditionalOperation(
                        condition = { it.currentPlayerNumber != currentPlayerNumber },
                        conditionalEvent = GameSimpleOperation.SWITCH_PLAYER,
                        context = BranchContext.NONE
                    )
                ).reversed(),
                context = BranchContext.NONE
            )
        }

        // TODO: memoize?
        val OPPONENT_HAND_REDRAW: (PlayerNumber, List<Card>) -> StackOperation = ::opponentHandRedraw

    }
}