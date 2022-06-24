package policies.rollout.jansen_tollisen

import engine.*
import engine.Player
import policies.rollout.randomPolicy

fun epsilonHeuristicGreedyPolicy(
    state: GameState,
    player: Player,
    context: ChoiceContext,
    choices: CardChoices
): Decision {

    val epsilon = 15

    // TODO: make sure this changes
    val random = (0..100).random()

    return if(random > epsilon) {
        randomPolicy(state, player, context, choices)
    } else {
        heuristicGreedyPolicy(state, player, context, choices)
    }

}