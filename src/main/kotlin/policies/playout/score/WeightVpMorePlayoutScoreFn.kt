package policies.playout.score

import engine.GameState
import engine.player.PlayerNumber

object WeightVpMorePlayoutScoreFn: PlayoutScoreFn {

    override val name = "weightVpMorePlayoutScoreFn"

    override fun invoke(state: GameState): Map<PlayerNumber, Double> {
        return state.players.associate { player ->
            val opponentVp = player.playerNumber.getOpponent(state).vp
            player.playerNumber to (player.vp - opponentVp) / 25.0 + if (player.vp > opponentVp) 1.0 else -1.0
        }
    }
}