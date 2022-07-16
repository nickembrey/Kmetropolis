package policies.utility

import engine.*
import engine.card.Card
import policies.Policy
import policies.PolicyName

// TODO: make this only handle actions and use a separate policy for treasures wherever this is being used for treasures
class MPPAFPolicy: Policy() {
    override val name = PolicyName("MPPAFPolicy")
    override fun shutdown() = Unit
    override fun policy(
        state: GameState,
        choices: CardChoices
    ): Card? {
        if (state.context == ChoiceContext.ACTION) {
            val addActionsCard = choices.filterNotNull().firstOrNull { it.addActions > 0 }   // TODO:
            if (addActionsCard != null) {
                return addActionsCard
            }
        }

        return FirstChoicePolicy().policy(state, choices)
    }
}

