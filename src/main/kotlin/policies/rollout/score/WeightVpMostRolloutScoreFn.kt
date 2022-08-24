package policies.rollout.score

import engine.GameState
import engine.player.PlayerNumber

object WeightVpMostRolloutScoreFn: RolloutScoreFn {

    override val name = "weightVpMostPlayoutScoreFn"

    override fun invoke(state: GameState): Map<PlayerNumber, Double> {
        return state.players.associate { player ->
            val opponentVp = player.playerNumber.getOpponent(state).vp
            player.playerNumber to (player.vp - opponentVp) / 10.0 + if (player.vp > opponentVp) 1.0 else 0.0
        }
    }
}