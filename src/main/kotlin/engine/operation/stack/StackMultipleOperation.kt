package engine.operation.stack

import engine.GameEvent
import engine.branch.BranchContext

class StackMultipleOperation(
    val events: List<GameEvent>,
    override val context: BranchContext,
): StackOperation