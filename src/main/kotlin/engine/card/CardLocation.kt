package engine.card

enum class CardLocation {
    SUPPLY,
    TRASH,
    PLAYER_ONE_IN_PLAY,
    PLAYER_TWO_IN_PLAY,
    PLAYER_ONE_HAND,
    PLAYER_TWO_HAND,
    PLAYER_ONE_DISCARD,
    PLAYER_TWO_DISCARD,
    PLAYER_ONE_TOPDECK,
    PLAYER_TWO_TOPDECK; // TODO: drawing is just a move from topdeck to hand, right?
}