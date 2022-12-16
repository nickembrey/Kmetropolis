package policies.mcts.rollout.score

import engine.GameState

interface RolloutScoreFn: (GameState) -> Pair<Double, Double> {
    val name: String
}