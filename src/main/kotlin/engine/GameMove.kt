package engine

import engine.card.Card
import engine.player.PlayerTag

// TODO: draw is still not integrated
data class GameMove(
    val playerTag: PlayerTag,
    val type: GameMoveType,
    val card: Card
)