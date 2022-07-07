package mcts

import engine.PlayerNumber

data class RolloutResult(
    val index: Int,
    val scores: Map<PlayerNumber, Double>
    )