package engine

import engine.branch.BranchContext

interface ContextBearer {
    val context: BranchContext
}