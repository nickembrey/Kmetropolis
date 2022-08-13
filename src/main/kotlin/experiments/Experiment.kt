package experiments

import policies.heuristic.SingleWitchV3Policy
import policies.mcts.DefaultMCTSPolicy
import policies.playout.GreenRolloutPolicy
import policies.playout.score.WeightVpMorePlayoutScoreFn

interface Experiment {
    fun run(times: Int)

    companion object {
        val DEFAULT_EXPERIMENT = SimpleExperiment(
            policy1 = DefaultMCTSPolicy(
                cParameter = 1.0,
                rollouts = 1000,
                GreenRolloutPolicy(),
                WeightVpMorePlayoutScoreFn,
                false),
            policy2 = SingleWitchV3Policy()
        )
    }
}