package policies.rollout.jansen_tollisen

import engine.*
import policies.Policy
import policies.rollout.randomPolicy

object epsilonHeuristicGreedyPolicy : Policy {
    override val name: String = "EpsilonHeuristicGreedyPolicy"
    override fun policy(
        state: GameState,
        choices: CardChoices
    ): Card? {

        val epsilon = 15

        // TODO: make sure this changes
        val random = (0..100).random()

        return if(random > epsilon) {
            randomPolicy.policy(state, choices)
        } else {
            heuristicGreedyPolicy.policy(state, choices)
        }

    }
}

