package policies.jansen_tollisen

import engine.*
import engine.branch.Branch
import engine.branch.BranchSelection
import policies.Policy
import policies.PolicyName
import policies.mcts.rollout.RandomPolicy

class EpsilonHeuristicGreedyPolicy : Policy() {

    companion object {
        val randomPolicy = RandomPolicy()
        val heuristicGreedyPolicy = HeuristicGreedyPolicy()
    }

    override val name: PolicyName = PolicyName("EpsilonHeuristicGreedyPolicy")
    override fun finally() = Unit
    override fun policy(
        state: GameState,
        branch: Branch
    ): BranchSelection {

        val epsilon = 15

        val random = (0..100).random()

        return if(random < epsilon) {
            randomPolicy(state, branch)
        } else {
            heuristicGreedyPolicy(state, branch)
        }

    }
}

