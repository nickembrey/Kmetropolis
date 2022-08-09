package engine.operation.stack

import engine.ContextBearer
import engine.GameState
import engine.branch.BranchContext
import engine.card.CardLocation
import engine.operation.Operation
import engine.operation.property.ReadPropertyOperation
import engine.operation.property.SetToPropertyOperation
import engine.operation.state.game.GameSimpleOperation
import engine.operation.state.player.PlayerMoveCardOperation
import engine.player.PlayerNumber
import util.memoize
import util.memoize2

interface StackOperation: Operation, ContextBearer {
    companion object {

        private fun opponentHandRedraw(
            currentPlayerNumber: PlayerNumber,
            otherPlayerHandSize: Int
        ): StackOperation {
            return StackMultipleOperation(
                events = listOf(
                    StackConditionalOperation(
                        condition = { it.currentPlayerNumber == currentPlayerNumber },
                        conditionalEvent = GameSimpleOperation.SWITCH_PLAYER,
                        context = BranchContext.NONE
                    ), StackSimpleOperation.DISCARD_ALL
                ).plus(
                    List(otherPlayerHandSize) { BranchContext.DRAW }
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
        val OPPONENT_HAND_REDRAW: (PlayerNumber, Int) -> StackOperation = ::opponentHandRedraw.memoize2()

    }
}