package engine.operation.stack

import engine.branch.BranchContext

enum class StackSimpleOperation: StackOperation {
    SKIP_CONTEXT;

    override val context: BranchContext = BranchContext.NONE
}