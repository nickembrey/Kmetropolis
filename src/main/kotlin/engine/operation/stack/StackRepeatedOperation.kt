package engine.operation.stack

import engine.GameState
import engine.GameEvent
import engine.branch.BranchContext

class StackRepeatedOperation(
    val repeatFn: (GameState) -> Int,
    val repeatedEvent: GameEvent,
    override val context: BranchContext
): StackOperation