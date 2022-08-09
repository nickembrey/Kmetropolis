package engine.operation.stack

import engine.GameState
import engine.GameEvent
import engine.branch.BranchContext
import engine.card.Card
import engine.operation.Operation.Companion.NO_OP
import engine.operation.property.SetFromPropertyOperation
import engine.operation.property.SetToPropertyOperation
import engine.operation.state.game.GameSimpleOperation

data class StackConditionalOperation(
    val condition: (GameState) -> Boolean,
    val conditionalEvent: GameEvent,
    val otherwiseEvent: GameEvent = NO_OP,
    val loopOnTrue: Boolean = false,
    override val context: BranchContext
): StackOperation