package policies.rollout.jansen_tollisen

import engine.*
import engine.Player

val heuristicGreedyPolicy = fun(
    state: GameState,
    player: Player,
    context: ChoiceContext,
    choice: Choice
): Decision {
    return when(context) {
        ChoiceContext.ACTION -> { // TODO: unchecked cast

            // order by whether it has actions first, then by cost (MPPAF+)
            // TODO: quick test
            val index = if(choice.isNotEmpty()) {
                val card = (choice as List<Card>).sortedWith(compareBy( { it.addActions }, { it.cost }))[0]
                choice.indexOf(card)
            } else {
                0
            }

            Decision(choice, context, index)
        }
        ChoiceContext.BUY -> { // TODO: unchecked cast
            val card = (choice as List<Card>).filter { it != Card.CURSE }.sortedWith(compareBy { it.cost })[0]
            Decision(choice, context, choice.indexOf(card))
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