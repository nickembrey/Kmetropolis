package engine.operation.stack.game

import engine.branch.BranchContext
import engine.operation.GameOperation
import engine.operation.stack.StackOperation

enum class GameCompoundOperation: StackOperation, GameOperation {
    NEXT_PHASE;

    override val context: BranchContext = BranchContext.NONE
}