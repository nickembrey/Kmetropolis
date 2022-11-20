package experiments

import policies.jansen_tollisen.SingleWitchPolicy
import policies.heuristic.SingleWitchV3Policy
import policies.mcts.MCTSPolicy
import policies.mcts.node.DefaultNodeValueFn
import policies.mcts.rollout.RandomPolicy
import policies.mcts.rollout.score.DefaultRolloutScoreFn
import policies.mcts.rollout.score.SquareDifferenceRolloutScoreFn

interface Experiment {
    fun run(times: Int): ExperimentResult

    companion object {
//        val INPUT_EXPERIMENT = SimpleExperiment(
//            policy1 = MCTSPolicy(
//                cParameter = 0.7,
//                rollouts = 10000,
//                rolloutPolicy = RandomPolicy(),
//                rolloutScoreFn = DefaultRolloutScoreFn,
//                nodeValueFn = DefaultNodeValueFn
//            ),
//            policy2 = InputPolicy()
//        )
        val DEFAULT_EXPERIMENT_1K = SimpleExperiment(
            policy1 = MCTSPolicy(
                cParameter = 0.7,
                rollouts = 1000,
                rolloutPolicy = RandomPolicy(),
                rolloutScoreFn = DefaultRolloutScoreFn,
                nodeValueFn = DefaultNodeValueFn
            ),
            policy2 = SingleWitchV3Policy()
        )
        val DEFAULT_EXPERIMENT_10K = SimpleExperiment(
            policy1 = MCTSPolicy(
                cParameter = 0.7,
                rollouts = 10000,
                rolloutPolicy = RandomPolicy(),
                rolloutScoreFn = DefaultRolloutScoreFn,
                nodeValueFn = DefaultNodeValueFn
            ),
            policy2 = SingleWitchV3Policy()
        )
        val EASY_EXPERIMENT_1K = SimpleExperiment(
            policy1 = MCTSPolicy(
                cParameter = 0.7,
                rollouts = 1000,
                rolloutPolicy = RandomPolicy(),
                rolloutScoreFn = DefaultRolloutScoreFn,
                nodeValueFn = DefaultNodeValueFn
            ),
            policy2 = SingleWitchPolicy()
        )
        val EASY_SQUARE_DIFFERENCE_EXPERIMENT_1K = SimpleExperiment(
            policy1 = MCTSPolicy(
                cParameter = 0.7,
                rollouts = 1000,
                rolloutPolicy = RandomPolicy(),
                rolloutScoreFn = SquareDifferenceRolloutScoreFn,
                nodeValueFn = DefaultNodeValueFn
            ),
            policy2 = SingleWitchPolicy()
        )
        val EASY_EXPERIMENT_10K = SimpleExperiment(
            policy1 = MCTSPolicy(
                cParameter = 0.7,
                rollouts = 10000,
                rolloutPolicy = RandomPolicy(),
                rolloutScoreFn = DefaultRolloutScoreFn,
                nodeValueFn = DefaultNodeValueFn
            ),
            policy2 = SingleWitchPolicy()
        )
    }
}