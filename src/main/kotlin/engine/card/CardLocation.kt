package engine.card

enum class CardLocation {
    DECK,
    SUPPLY,
    TRASH,
    IN_PLAY,
    HAND,
    DISCARD,
    TOPDECK;

// TODO: drawing is just a move from topdeck to hand, right?
}