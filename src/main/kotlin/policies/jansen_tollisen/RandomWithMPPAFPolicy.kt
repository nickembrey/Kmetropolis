package policies.jansen_tollisen

import engine.*
import engine.card.Card
import policies.Policy
import policies.PolicyName

class RandomWithMPPAFPolicy : Policy() {
    override val name = PolicyName("randomWithMPPAFPolicy")
    override fun policy(
        state: GameState,
        choices: CardChoices
    ): Card? {
        // must play plus actions first (MPPAF)
        if(state.context == ChoiceContext.ACTION) {
            val addActionsCard = choices.filterNotNull().firstOrNull { it.addActions > 0 }   // TODO:
            if (addActionsCard != null) { // TODO: will it ever be null?
                return addActionsCard
            }
        } else if(state.context == ChoiceContext.TREASURE) {
            return choices[0]
        }

        return choices.random()
    }
}

