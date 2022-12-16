package policies.delegates.action

import engine.GameState
import engine.branch.*
import engine.card.CardType
import policies.Policy
import policies.PolicyName

class MPPAFPolicy: Policy() {
    override val name = PolicyName("MPPAFPolicy")
    override fun finally() = Unit
    override fun policy(
        state: GameState,
        branch: Branch
    ): BranchSelection {
        return if (branch.context == BranchContext.CHOOSE_ACTION) {
            val options = branch.getOptions(state)
                .filterIsInstance<ActionSelection>()
                .map { it.card }
                .sortedWith(compareBy { it.cost })
                .sortedWith(
                    compareBy(
                        { it.type == CardType.ACTION },
                        { it.addActions > 0 }
                    )).reversed()
            if(options.isEmpty()) {
                SpecialBranchSelection.SKIP
            } else {
                ActionSelection(card = options[0])
            }
        } else {
            throw IllegalArgumentException()
        }
    }
}

