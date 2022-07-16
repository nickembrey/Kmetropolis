package policies.jansen_tollisen

import engine.*
import engine.card.Card
import policies.Policy
import policies.PolicyName
import policies.utility.RandomPolicy

class EpsilonHeuristicGreedyPolicy : Policy() {

    companion object {
        val randomPolicy = RandomPolicy()
        val heuristicGreedyPolicy = HeuristicGreedyPolicy()
    }

    override val name: PolicyName = PolicyName("EpsilonHeuristicGreedyPolicy")
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

