package policies.mcts.rollout.score

import engine.GameState
import engine.player.PlayerNumber

// TODO: these are really sloppy / non-automated

interface RolloutScoreFn: (GameState) -> Pair<Double, Double> {
    val name: String
}