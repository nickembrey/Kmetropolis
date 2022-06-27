package engine

typealias Board = MutableMap<Card, Int>

val defaultBoard = mutableMapOf(
    Card.FESTIVAL to 10,
    Card.WITCH to 10,
    Card.MARKET to 10,
    Card.LABORATORY to 10,
    Card.SMITHY to 10,
    Card.MONEYLENDER to 10,
    Card.MILITIA to 10,
    Card.CHAPEL to 10,
    Card.VILLAGE to 10,
    Card.WORKSHOP to 10,

    Card.GOLD to 30,
    Card.SILVER to 40,
    Card.COPPER to 46,

    Card.PROVINCE to 8,
    Card.DUCHY to 8,
    Card.ESTATE to 8,

    Card.CURSE to 10,
)