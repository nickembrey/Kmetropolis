package experiments

import policies.heuristic.SingleWitchV3Policy
import policies.mcts.DefaultMCTSPolicy
import policies.rollout.RandomPolicy
import policies.rollout.score.DefaultRolloutScoreFn

interface Experiment {
    fun run(times: Int): ExperimentResult

    companion object {
        val DEFAULT_EXPERIMENT = SimpleExperiment(
            policy1 = DefaultMCTSPolicy(
                cParameter = 1.0,
                rollouts = 1000,
                RandomPolicy(),
                DefaultRolloutScoreFn
            ),
            policy2 = SingleWitchV3Policy()
        )
    }
}