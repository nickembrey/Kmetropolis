package mcts

import engine.player.PlayerNumber

data class RolloutResult(
    val index: Int,
    val scores: Map<PlayerNumber, Double>
    )