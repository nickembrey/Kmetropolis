package engine.operation.stack

import engine.branch.BranchContext

enum class StackSimpleOperation: StackOperation {
    ADD_PHASE_OPERATIONS, SKIP_CONTEXT, SHUFFLE, CHECK_GAME_OVER, CLEANUP_ALL, DISCARD_ALL;

    override val context: BranchContext = BranchContext.NONE
}