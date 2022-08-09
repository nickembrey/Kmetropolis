package policies.utility

import engine.*
import engine.branch.BranchSelection
import policies.Policy
import policies.PolicyName

class RandomPolicy : Policy() {
    override val name = PolicyName("randomPolicy")
    override fun shutdown() = Unit
    override fun policy(state: GameState): BranchSelection {
        return state.context.toOptions(state).random()
    }
}