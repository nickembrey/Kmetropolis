package policies.rollout.jansen_tollisen

import engine.*
import engine.Player
import policies.policy.heuristicGreedyPolicy
import policies.policy.randomPolicy

fun _epsilonHeuristicGreedyPolicy(
    state: GameState,
    choices: CardChoices
): Card? {

    val epsilon = 15

    // TODO: make sure this changes
    val random = (0..100).random()

    return if(random > epsilon) {
        randomPolicy(state, choices)
    } else {
        heuristicGreedyPolicy(state, choices)
    }

}