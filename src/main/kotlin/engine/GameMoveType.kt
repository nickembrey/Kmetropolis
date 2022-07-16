package engine


// TODO: add shuffling?
enum class GameMoveType(
    val verb: String,
    val requireSuccess: Boolean = false // does the move need to be validated to be considered complete?
) {
    BUY("buys"),
    DISCARD("discards"),
    DRAW("draws", true),
    GAIN("gains", true),
    PLAY("plays"),
    TOPDECK("topdecks"),
    TRASH("trashes");
}