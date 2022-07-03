package engine

enum class GameMove(val verb: String) {
    BUY("buys"),
    GAIN("gains"),
    PLAY("plays"),
    TRASH("trashes"),
    DISCARD("discards");
}