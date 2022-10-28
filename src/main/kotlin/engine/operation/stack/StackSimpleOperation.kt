package engine.operation.stack

import engine.branch.BranchContext

enum class StackSimpleOperation: StackOperation {
    SKIP_CONTEXT, SHUFFLE, CLEANUP_ALL, DISCARD_ALL;

    override val context: BranchContext = BranchContext.NONE
}