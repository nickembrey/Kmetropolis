package policies.rollout.jansen_tollisen

import engine.*
import engine.Player

fun _heuristicGreedyPolicy(
    state: GameState,
    choices: CardChoices
): Card? {
    return when(state.context) {
        ChoiceContext.ACTION -> { // TODO: unchecked cast

            // order by whether it has actions first, then by cost (MPPAF)
            // TODO: quick test
            choices.sortedWith(compareBy( { it?.addActions }, { it?.cost }))[0] // TODO: unchecked cast
        }
        ChoiceContext.TREASURE -> choices[0] // TODO: this is wrong, see MCTS policy
        ChoiceContext.BUY -> { // TODO: unchecked cast
            // TODO: weird corner case where all coppers and all curses are gone?
            choices.filter { it != Card.CURSE }.sortedWith(compareBy { it?.cost })[0] // TODO: make sure null will never be first?
        }
        ChoiceContext.MILITIA -> throw NotImplementedError()
        ChoiceContext.WORKSHOP -> throw NotImplementedError()
        ChoiceContext.CHAPEL -> throw NotImplementedError()
    }
}