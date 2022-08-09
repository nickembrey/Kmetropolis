package policies.jansen_tollisen

import engine.*
import engine.branch.BranchSelection
import policies.Policy
import policies.PolicyName
import policies.utility.RandomPolicy

class EpsilonHeuristicGreedyPolicy : Policy() {

    companion object {
        val randomPolicy = RandomPolicy()
        val heuristicGreedyPolicy = HeuristicGreedyPolicy()
    }

    override val name: PolicyName = PolicyName("EpsilonHeuristicGreedyPolicy")
    override fun shutdown() = Unit
    override fun policy(state: GameState): BranchSelection {

        val epsilon = 15

        // TODO: make sure this changes
        val random = (0..100).random()

        return if(random < epsilon) {
            randomPolicy.policy(state)
        } else {
            heuristicGreedyPolicy.policy(state)
        }

    }
}
