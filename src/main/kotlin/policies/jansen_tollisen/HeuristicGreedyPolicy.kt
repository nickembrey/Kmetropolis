package policies.jansen_tollisen

import engine.*
import engine.card.Card
import engine.branch.BranchContext
import engine.branch.BranchSelection
import engine.branch.SpecialBranchSelection
import policies.Policy
import policies.PolicyName
import policies.utility.RandomPolicy

class HeuristicGreedyPolicy : Policy() {

    companion object {
        val randomPolicy = RandomPolicy()
    }

    override val name = PolicyName("heuristicGreedyPolicy")
    override fun shutdown() = Unit
    override fun policy(state: GameState): BranchSelection {

        val options = state.context.toOptions(state)

        return when(state.context) {
            BranchContext.CHOOSE_ACTION -> {
                // order by number of actions first, then by cost
                val cardSelection = options.filterIsInstance<Card>()
                    .sortedWith(compareBy ( { it.addActions }, { it.cost }) ).reversed()
                if(cardSelection.isNotEmpty()) {
                    return cardSelection[0]
                } else {
                    SpecialBranchSelection.SKIP
                }
            }
            BranchContext.CHOOSE_TREASURE -> {
                return options.firstOrNull { it is Card } ?: SpecialBranchSelection.SKIP
            }
            BranchContext.CHOOSE_BUY -> {
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
            else -> randomPolicy.policy(state)
        }
    }
}

