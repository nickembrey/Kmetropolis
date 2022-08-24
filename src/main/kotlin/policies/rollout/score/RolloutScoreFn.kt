package policies.rollout.score

import engine.GameState
import engine.player.PlayerNumber

// TODO: these are really sloppy / non-automated

interface RolloutScoreFn: (GameState) -> Map<PlayerNumber, Double> {
    val name: String
}