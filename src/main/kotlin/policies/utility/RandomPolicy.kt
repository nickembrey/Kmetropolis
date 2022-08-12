package policies.utility

import engine.*
import engine.branch.Branch
import engine.branch.BranchContext
import engine.branch.BranchSelection
import policies.Policy
import policies.PolicyName
import policies.delegates.draw.RandomDrawPolicy

class RandomPolicy : Policy() {
    override val name = PolicyName("randomPolicy")
    override fun shutdown() = Unit

    private val drawPolicy = RandomDrawPolicy()
    override fun policy(
        state: GameState,
        branch: Branch
    ): BranchSelection {
        return when(branch.context) {
            BranchContext.DRAW -> drawPolicy.policy(state, branch)
            else -> branch.getOptions(state).random()
        }
    }
}