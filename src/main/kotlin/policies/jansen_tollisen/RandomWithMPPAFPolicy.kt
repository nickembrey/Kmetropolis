package policies.jansen_tollisen

import engine.*
import engine.branch.*
import engine.card.Card
import policies.Policy
import policies.PolicyName

class RandomWithMPPAFPolicy : Policy() {
    override val name = PolicyName("randomWithMPPAFPolicy")
    override fun finally() = Unit
    override fun policy(
        state: GameState,
        branch: Branch
    ): BranchSelection {

        val options = branch.getOptions(state)

        // must play plus actions first (MPPAF) // TODO: when
        if(branch.context == BranchContext.CHOOSE_ACTION) {
            val actionsCards = options.filterIsInstance<Card>()
            if(actionsCards.isNotEmpty()) {
                val addActionsCard = actionsCards.firstOrNull { it.addActions > 0 }
                return addActionsCard?.let { ActionSelection(card = it) } ?: ActionSelection(card = actionsCards.first())
                // TODO: if we don't have plus actions cards, we should put it thru the policy
            }
        } else if(branch.context == BranchContext.CHOOSE_TREASURE) {
            return options.firstOrNull { it is TreasureSelection } ?: SpecialBranchSelection.SKIP
        }

        return options.random()
    }
}

