package policies.playout.score

import engine.GameState
import engine.player.PlayerNumber

interface PlayoutScoreFn: (GameState) -> Map<PlayerNumber, Double> {
    val name: String
}