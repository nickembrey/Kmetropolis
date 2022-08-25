package experiments

import engine.GameResult
import engine.card.Card
import engine.player.PlayerNumber

class PlayerGameSummary(
    val playerNumber: PlayerNumber,
    val deck: List<Card>,
    val result: GameResult,
    val vp: Int,
)