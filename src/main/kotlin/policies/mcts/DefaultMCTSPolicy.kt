package policies.mcts

import policies.CurrentThreadExecutor
import policies.Policy
import policies.PolicyName
import policies.rollout.score.RolloutScoreFn

class DefaultMCTSPolicy(
    cParameter: Double,
    rollouts: Int,
    rolloutPolicy: Policy,
    rolloutScoreFn: RolloutScoreFn
): MCTSPolicy( // TODO: easier way?
    cParameter = cParameter,
    rollouts = rollouts,
    rolloutPolicy = rolloutPolicy,
    rolloutScoreFn = rolloutScoreFn
) {
    override val name = PolicyName(
        "defaultMCTSPolicy ($cParameter, $rollouts, ${rolloutPolicy.name}, ${rolloutScoreFn.name})")

    override val stateCopies: Int = 1
    override val executor = CurrentThreadExecutor()

    override fun shutdown() {}
}