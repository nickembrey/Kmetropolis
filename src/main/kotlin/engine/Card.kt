package engine

enum class Card(
    val id: Int,
    val type: CardType,
    val cost: Int,
    val addCoins: Int = 0,
    val addCards: Int = 0,
    val addActions: Int = 0,
    val addBuys: Int = 0,
    val vp: Int = 0,
    val effectList: List<CardEffect> = listOf(),
) {
    FESTIVAL(1, type = CardType.ACTION, cost = 5, addCoins = 2, addActions = 2, addBuys = 1),
    WITCH(2, type = CardType.ACTION, cost = 5, addCards = 2, effectList = listOf(CardEffect.WitchEffect)),
    MARKET(3, type = CardType.ACTION, cost = 5, addCards = 1, addActions = 1, addBuys = 1),
    LABORATORY(4, type = CardType.ACTION, cost = 5, addCards = 2, addActions = 1),
    SMITHY(5, type = CardType.ACTION, cost = 4, addCards = 3),
    MONEYLENDER(6, type = CardType.ACTION, cost = 4, effectList = listOf(CardEffect.MoneylenderEffect)),
    MILITIA(7, type = CardType.ACTION, cost = 4, addCoins = 2, effectList = listOf(CardEffect.MilitiaEffect)),
    CHAPEL(8, type = CardType.ACTION, cost = 2, effectList = listOf(CardEffect.ChapelEffect)),
    VILLAGE(9, type = CardType.ACTION, cost = 3, addCards = 1, addActions = 2),
    WORKSHOP(10, type = CardType.ACTION, cost = 3, effectList = listOf(CardEffect.WorkshopEffect)),

    GOLD(11, type = CardType.TREASURE, cost = 6, addCoins = 3),
    SILVER(12, type = CardType.TREASURE, cost = 3, addCoins = 2),
    COPPER(13, type = CardType.TREASURE, cost = 0, addCoins = 1),

    PROVINCE(14, type = CardType.OTHER, cost = 8, vp = 6),
    DUCHY(15, type = CardType.OTHER, cost = 5, vp = 3),
    ESTATE(16, type = CardType.OTHER, cost = 2, vp = 1),

    CURSE(17, type = CardType.OTHER, cost = 0, vp = -1);

    companion object {
        fun fromCardId(cardId: Int): Card {
            return values().first { it.id == cardId }
        }
    }
}