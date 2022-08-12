package policies.jansen_tollisen

import engine.*
import engine.branch.Branch
import engine.card.Card
import engine.branch.BranchContext
import engine.branch.BranchSelection
import engine.branch.SpecialBranchSelection
import policies.Policy
import policies.PolicyName
import policies.delegates.action.MPPAFPolicy
import policies.utility.RandomPolicy

class HeuristicGreedyPolicy : Policy() {

    companion object {
        val randomPolicy = RandomPolicy()
    }

    override val name = PolicyName("heuristicGreedyPolicy")
    override fun shutdown() = Unit
    override fun policy(
        state: GameState,
        branch: Branch
    ): BranchSelection {

        val options = branch.getOptions(state)

        return when(state.context) {
            BranchContext.CHOOSE_ACTION -> {
                MPPAFPolicy().policy(state, branch)
            }
            BranchContext.CHOOSE_TREASURE -> {
                return options.firstOrNull { it is Card } ?: SpecialBranchSelection.SKIP
            }
            BranchContext.CHOOSE_BUYS -> {
                // TODO: weird corner case where all coppers and all curses are gone?
                val buySelections = options
                    .filterIsInstance<Card>()
                    .filter { it != Card.CURSE }
                    .sortedWith(compareByDescending { it.cost })
                return if(buySelections.isNotEmpty()) {
                    buySelections[0]
                } else {
                    SpecialBranchSelection.SKIP
                }
            }
            else -> randomPolicy.policy(state, branch)
        }
    }
}

