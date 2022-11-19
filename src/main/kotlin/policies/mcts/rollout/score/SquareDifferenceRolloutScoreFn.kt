package policies.mcts.rollout.score

import com.marcinmoskala.math.pow
import engine.GameState
import engine.player.PlayerNumber

object SquareDifferenceRolloutScoreFn: RolloutScoreFn {

    override val name = "SquareDifferenceRolloutScoreFn"

    private fun getScore(ownVp: Int, opponentVp: Int): Double =
        if (ownVp > opponentVp) (ownVp - opponentVp).pow(2).toDouble() else if(ownVp == opponentVp) 0.75 else 0.0

    override fun invoke(state: GameState): Pair<Double, Double> {
        val p1 = PlayerNumber.PLAYER_ONE.getPlayer(state).vp
        val p2 = PlayerNumber.PLAYER_TWO.getPlayer(state).vp
        return Pair(
            getScore(p1, p2),
            getScore(p2, p1)
        )
    }
}