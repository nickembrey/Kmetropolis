package policies.mcts.rollout.score

import engine.GameState
import engine.player.PlayerNumber

object DefaultRolloutScoreFn: RolloutScoreFn {

    override val name = "defaultPlayoutScoreFn"

    private fun getScore(ownVp: Int, opponentVp: Int): Double =
        if (ownVp > opponentVp) 1.0 else if(ownVp == opponentVp) 0.5 else 0.0

    override fun invoke(state: GameState): Pair<Double, Double> {
        val p1 = PlayerNumber.PLAYER_ONE.getPlayer(state).vp
        val p2 = PlayerNumber.PLAYER_TWO.getPlayer(state).vp
        return Pair(
            getScore(p1, p2),
            getScore(p2, p1)
        )

    }
}