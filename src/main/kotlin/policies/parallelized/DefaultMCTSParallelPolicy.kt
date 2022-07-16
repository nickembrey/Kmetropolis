package policies.parallelized

import policies.MCTSPolicy
import policies.PolicyName
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class DefaultMCTSParallelPolicy(
    cParameter: Double,
    rollouts: Int
): MCTSPolicy(
    cParameter = cParameter,
    rollouts = rollouts
) {

    override val name = PolicyName("defaultMCTSParallelPolicy")

    override val executor: ExecutorService = Executors.newWorkStealingPool()

    override fun shutdown() {
        executor.shutdown()
    }
}