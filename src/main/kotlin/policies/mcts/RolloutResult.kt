package policies.mcts

import engine.player.PlayerNumber

interface ExecutionResult {
}

data class RolloutSelection(val index: Int): ExecutionResult

data class RolloutResult(
    val index: Int,
    val scores: Map<PlayerNumber, Double>): ExecutionResult

object NO_ROLLOUT: ExecutionResult