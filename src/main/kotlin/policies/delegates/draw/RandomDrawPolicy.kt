package policies.delegates.draw

import engine.*
import engine.branch.Branch
import engine.branch.BranchContext
import engine.branch.BranchSelection
import engine.branch.DrawSelection
import policies.Policy
import policies.PolicyName
import kotlin.random.Random

class RandomDrawPolicy : Policy() {
    override val name = PolicyName("randomPolicy")
    override fun finally() = Unit
    override fun policy(
        state: GameState,
        branch: Branch
    ): BranchSelection {
        if (branch.context != BranchContext.DRAW) {
            throw IllegalStateException()
        } else {
            return branch.getOptions(state).single()
        }
    }
}