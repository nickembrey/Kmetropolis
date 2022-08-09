package engine.operation.state.player

// TODO: get rid of

enum class PlayerOperationType(val verb: String) {
    INCREMENT_PROPERTY("adds"),
    DECREMENT_PROPERTY("subtracts"),
    BUY("buys"),
    CLEANUP("cleans up"), // i.e., puts into discard from play
    UNDO_CLEANUP("undoes cleanup on"),
    DISCARD("discards"),
    UNDO_DISCARD("undoes discard on"),
    DRAW("draws"),
    GAIN("gains"),
    UNDO_GAIN("undoes gain on"),
    PLAY("plays"),
    SHUFFLE("shuffles"),
    TOPDECK_FROM_DISCARD("topdecks from discard"),
    TRASH("trashes"),
    UNDO_TRASH("undoes trash on"),
    GAME_OVER("ends the game"),
}