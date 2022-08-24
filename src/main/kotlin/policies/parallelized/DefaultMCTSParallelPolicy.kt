package policies.parallelized

import policies.Policy
import policies.PolicyName
import policies.mcts.MCTSPolicy
import policies.rollout.score.RolloutScoreFn
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class DefaultMCTSParallelPolicy(
    cParameter: Double,
    rollouts: Int,
    rolloutPolicy: Policy,
    rolloutScoreFn: RolloutScoreFn
): MCTSPolicy(
    cParameter = cParameter,
    rollouts = rollouts,
    rolloutPolicy = rolloutPolicy,
    rolloutScoreFn = rolloutScoreFn
) {

    override val name = PolicyName("defaultMCTSParallelPolicy")

    override val executor: ExecutorService = Executors.newWorkStealingPool()
    override val stateCopies: Int = 4

    override fun shutdown() {
        executor.shutdown()
    }
}