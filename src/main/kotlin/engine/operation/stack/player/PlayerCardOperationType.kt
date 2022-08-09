package engine.operation.stack.player

enum class PlayerCardOperationType(val verb: String) {
    DRAW("draws"),
    BUY("buys"),
    GAIN("gains"),
    PLAY("plays"),
    TRASH("trashes");
}