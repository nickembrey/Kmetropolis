package policies.jansen_tollisen

import engine.*
import engine.card.Card
import engine.branch.BranchContext
import engine.branch.BranchSelection
import engine.branch.SpecialBranchSelection
import policies.Policy
import policies.PolicyName

class RandomWithMPPAFPolicy : Policy() {
    override val name = PolicyName("randomWithMPPAFPolicy")
    override fun shutdown() = Unit
    override fun policy(state: GameState): BranchSelection {

        val options = state.context.toOptions(state)

        // must play plus actions first (MPPAF)
        if(state.context == BranchContext.CHOOSE_ACTION) {
            val actionsCards = options.filterIsInstance<Card>()
            if(actionsCards.isNotEmpty()) {
                val addActionsCard = actionsCards.firstOrNull { it.addActions > 0 }
                return addActionsCard ?: actionsCards.first()
                // TODO: if we don't have plus actions cards, we should put it thru the policy
            }
        } else if(state.context == BranchContext.CHOOSE_TREASURE) {
            return options.firstOrNull { it is Card } ?: SpecialBranchSelection.SKIP
        }

        return options.random()
    }
}

