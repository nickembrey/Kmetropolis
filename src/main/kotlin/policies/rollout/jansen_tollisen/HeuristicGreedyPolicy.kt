package policies.rollout.jansen_tollisen

import engine.*
import engine.Player

fun heuristicGreedyPolicy(
    state: GameState,
    player: Player,
    context: ChoiceContext,
    choices: CardChoices
): Decision {
    return when(context) {
        ChoiceContext.ACTION -> { // TODO: unchecked cast

            // order by whether it has actions first, then by cost (MPPAF)
            // TODO: quick test
            val card = (choices.choices as List<Card>).sortedWith(compareBy( { it.addActions }, { it.cost }))[0] // TODO: unchecked cast
            Decision(choices.choices.indexOf(card))
        }
        ChoiceContext.TREASURE -> {
            Decision(0)
        }
        ChoiceContext.BUY -> { // TODO: unchecked cast
            // TODO: weird corner case where all coppers and all curses are gone?
            val card = (choices as List<Card>).filter { it != Card.CURSE }.sortedWith(compareBy { it.cost })[0]
            Decision(choices.indexOf(card))
        }
        ChoiceContext.MILITIA -> {
            throw NotImplementedError()
        }
        ChoiceContext.WORKSHOP -> {
            throw NotImplementedError()
        }
        ChoiceContext.CHAPEL -> {
            throw NotImplementedError()
        }
    }
}