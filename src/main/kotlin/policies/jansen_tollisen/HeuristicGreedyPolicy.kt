package policies.jansen_tollisen

import engine.*
import engine.branch.*
import engine.card.Card
import policies.Policy
import policies.PolicyName
import policies.delegates.action.MPPAFPolicy
import policies.mcts.rollout.RandomPolicy

class HeuristicGreedyPolicy : Policy() {

    companion object {
        val randomPolicy = RandomPolicy()
    }

    override val name = PolicyName("heuristicGreedyPolicy")
    override fun finally() = Unit
    override fun policy(
        state: GameState,
        branch: Branch
    ): BranchSelection {

        val options = branch.getOptions(state)

        return when(branch.context) {
            BranchContext.CHOOSE_ACTION -> {
                MPPAFPolicy()(state, branch)
            }
            BranchContext.CHOOSE_TREASURE -> {
                return options.firstOrNull { it is TreasureSelection } ?: SpecialBranchSelection.SKIP
            }
            BranchContext.CHOOSE_BUY -> {
                val buySelections = options
                    .filterIsInstance<BuySelection>()
                    .filter { !it.cards.contains(Card.CURSE) }
                    .sortedWith(compareByDescending { it.cards.sumOf { card -> card.cost } })
                return if(buySelections.isNotEmpty()) {
                    buySelections[0]
                } else {
                    SpecialBranchSelection.SKIP
                }
            }
            else -> randomPolicy(state, branch)
        }
    }
}

