package policies.utility

import engine.*
import engine.branch.Branch
import engine.branch.BranchSelection
import policies.Policy
import policies.PolicyName

class FirstChoicePolicy: Policy() { // TODO: rethink now that choices is a set
    override val name = PolicyName("firstChoicePolicy")
    override fun shutdown() = Unit
    override fun policy(
        state: GameState,
        branch: Branch
    ): BranchSelection {
        val options = branch.getOptions(state)
        return options.first()
    }
}

