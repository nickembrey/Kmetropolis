package engine

enum class Card(
    val type: CardType,
    val cost: Int,
    val addCoins: Int = 0,
    val addCards: Int = 0,
    val addActions: Int = 0,
    val addBuys: Int = 0,
    val vp: Int = 0,
    val effectList: List<CardEffect> = listOf(),
) {
    FESTIVAL(type = CardType.ACTION, cost = 5, addCoins = 2, addActions = 2, addBuys = 1),
    WITCH(type = CardType.ACTION, cost = 5, addCards = 2, effectList = listOf(::witchEffect)),
    MARKET(type = CardType.ACTION, cost = 5, addCards = 1, addActions = 1, addBuys = 1),
    LABORATORY(type = CardType.ACTION, cost = 5, addCards = 2, addActions = 1),
    SMITHY(type = CardType.ACTION, cost = 4, addCards = 3),
    MONEYLENDER(type = CardType.ACTION, cost = 4, effectList = listOf(::moneylenderEffect)),
    MILITIA(type = CardType.ACTION, cost = 4, addCoins = 2, effectList = listOf(::militiaEffect)),
    CHAPEL(type = CardType.ACTION, cost = 2, effectList = listOf(::chapelEffect)),
    VILLAGE(type = CardType.ACTION, cost = 3, addCards = 1, addActions = 2),
    WORKSHOP(type = CardType.ACTION, cost = 3, effectList = listOf(::workshopEffect)),

    GOLD(type = CardType.TREASURE, cost = 6, addCoins = 3),
    SILVER(type = CardType.TREASURE, cost = 3, addCoins = 2),
    COPPER(type = CardType.TREASURE, cost = 0, addCoins = 1),

    PROVINCE(type = CardType.OTHER, cost = 8, vp = 6),
    DUCHY(type = CardType.OTHER, cost = 5, vp = 3),
    ESTATE(type = CardType.OTHER, cost = 2, vp = 1),

    CURSE(type = CardType.OTHER, cost = 0, vp = -1);
}