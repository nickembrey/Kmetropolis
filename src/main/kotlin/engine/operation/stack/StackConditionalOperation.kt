package engine.operation.stack

import engine.GameState
import engine.GameEvent
import engine.branch.BranchContext

data class StackConditionalOperation(
    val condition: (GameState) -> Boolean,
    val conditionalEvent: GameEvent,
    val otherwiseEvent: GameEvent,
    val loopOnTrue: Boolean = false,
    override val context: BranchContext
): StackOperation