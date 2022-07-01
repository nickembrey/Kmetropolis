package policies.rollout.jansen_tollisen

import engine.*

fun _randomPolicyWithMPPAF(
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