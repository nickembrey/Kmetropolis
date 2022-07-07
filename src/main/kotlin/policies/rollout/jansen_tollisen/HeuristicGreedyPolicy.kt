package policies.rollout.jansen_tollisen

import engine.*
import policies.Policy
import policies.PolicyName
import policies.rollout.RandomPolicy

class HeuristicGreedyPolicy : Policy() {

    companion object {
        val randomPolicy = RandomPolicy()
    }

    override val name = PolicyName("heuristicGreedyPolicy")
    override fun policy(
        state: GameState,
        choices: CardChoices
    ): Card? {
        return when(state.context) {
            ChoiceContext.ACTION -> {
                // order by number of actions first, then by cost
                choices.sortedWith(compareBy ( { it?.addActions }, { it?.cost }) ).reversed()[0]
            }
            ChoiceContext.TREASURE -> choices[0]
            ChoiceContext.BUY -> {
                // TODO: weird corner case where all coppers and all curses are gone?
                choices.filter { it != Card.CURSE }.sortedWith(compareByDescending { it?.cost })[0] // TODO: make sure null will never be first?
            }
            ChoiceContext.MILITIA -> throw NotImplementedError()
            ChoiceContext.WORKSHOP -> throw NotImplementedError()
            ChoiceContext.CHAPEL -> throw NotImplementedError()
            else -> randomPolicy.policy(state, choices)
        }
    }
}

