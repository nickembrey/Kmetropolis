package policies.mcts

import policies.CurrentThreadExecutor
import policies.Policy
import policies.PolicyName
import policies.playout.score.PlayoutScoreFn

class DefaultMCTSPolicy(
    cParameter: Double,
    rollouts: Int,
    rolloutPolicy: Policy,
    rolloutScoreFn: PlayoutScoreFn,
    useNBCWeights: Boolean
): MCTSPolicy( // TODO: easier way?
    cParameter = cParameter,
    rollouts = rollouts,
    rolloutPolicy = rolloutPolicy,
    rolloutScoreFn = rolloutScoreFn,
    useNBCWeights = useNBCWeights
) {
    override val name = PolicyName(
        "defaultMCTSPolicy ($cParameter, $rollouts, ${rolloutPolicy.name}, ${rolloutScoreFn.name}, $useNBCWeights)")

    override val stateCopies: Int = 1
    override val executor = CurrentThreadExecutor()

    override fun shutdown() {}
}