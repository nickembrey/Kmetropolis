package engine.operation.stack.player

enum class PlayerCardOperationType(val verb: String) {
    DRAW("draws"),
    BUY("buys"),
    GAIN("gains"),
    PLAY("plays"),
    PLAY_FROM_DISCARD("plays from discard"),
    TRASH("trashes");
}