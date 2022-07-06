package engine

// TODO: draw is still not integrated
data class GameMove(
    val playerTag: PlayerTag,
    val type: GameMoveType,
    val card: Card)