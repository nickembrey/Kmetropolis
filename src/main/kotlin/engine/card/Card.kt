package engine.card

enum class Card(
    val type: CardType,
    val cost: Int,
    val addActions: Int = 0,
    val addBuys: Int = 0,
    val addCards: Int = 0,
    val addCoins: Int = 0,
    val vp: Int = 0,
    val cardEffects: List<CardEffect> = listOf(),
) { // TODO: organize parameters
    CELLAR(type = CardType.ACTION, cost = 2, addActions = 1, cardEffects = listOf(CardEffect.CELLAR_EFFECT)),
    CHAPEL(type = CardType.ACTION, cost = 2, cardEffects = listOf(CardEffect.CHAPEL_EFFECT)),
    MOAT(type = CardType.ACTION, cost = 2, cardEffects = listOf(CardEffect.MOAT_EFFECT)),
    HARBINGER(type = CardType.ACTION, cost = 3, addActions = 1, addCards = 1, cardEffects = listOf(CardEffect.HARBINGER_EFFECT)),
//    MERCHANT(type = CardType.ACTION, cost = 3, addActions = 1, addCards = 1, effectList = listOf(::merchantEffect)), // TODO: probably not an effect?
//    VASSAL(type = CardType.ACTION, cost = 3, addCoins = 2, effectList = listOf(::vassalEffect)),
    VILLAGE(type = CardType.ACTION, cost = 3, addCards = 1, addActions = 2),
    WORKSHOP(type = CardType.ACTION, cost = 3, cardEffects = listOf(CardEffect.WORKSHOP_EFFECT)),
//    BUREAUCRAT(type = CardType.ACTION, cost = 4, effectList = listOf(::bureacratEffect)), // TODO: maybe two effects?
    MILITIA(type = CardType.ACTION, cost = 4, addCoins = 2, cardEffects = listOf(CardEffect.MILITIA_EFFECT)), // TODO: attack
    MONEYLENDER(type = CardType.ACTION, cost = 4, cardEffects = listOf(CardEffect.MONEYLENDER_EFFECT)),
//    POACHER(type = CardType.ACTION, cost = 4, addCards = 1, addActions = 1, effectList = listOf(::poacherEffect)),
    REMODEL(type = CardType.ACTION, cost = 4, cardEffects = listOf(CardEffect.REMODEL_EFFECT)),
    SMITHY(type = CardType.ACTION, cost = 4, addCards = 3),
//    THRONE_ROOM(type = CardType.ACTION, cost = 4, effectList = listOf(::throneRoomEffect)),
//    BANDIT(type = CardType.ACTION, cost = 5, effectList = listOf(::banditEffect)), // TODO: attack
//    COUNCIL_ROOM(type = CardType.ACTION, cost = 5, addCards = 4, effectList = listOf(::councilRoomEffect)),
    FESTIVAL(type = CardType.ACTION, cost = 5, addCoins = 2, addActions = 2, addBuys = 1),
    LABORATORY(type = CardType.ACTION, cost = 5, addCards = 2, addActions = 1),
//    LIBRARY(type = CardType.ACTION, cost = 5, effectList = listOf(::councilRoomEffect)),
    MARKET(type = CardType.ACTION, cost = 5, addCards = 1, addActions = 1, addBuys = 1),
//    MINE(type = CardType.ACTION, cost = 5, effectList = listOf(::mineEffect)),
//    SENTRY(type = CardType.ACTION, cost = 5, addActions = 1, addCards = 1, effectList = listOf(::sentryEffect)),
    WITCH(type = CardType.ACTION, cost = 5, addCards = 2, cardEffects = listOf(CardEffect.WITCH_EFFECT)), // TODO: figure out how to handle attacks and moat blocks
//    ARTISAN(type = CardType.ACTION, cost = 6, effectList = listOf(::artisanEffect)),
//
//    CHANCELLOR(type = CardType.ACTION, cost = 3, addCoins = 2, effectList = listOf(::chancellorEffect)),
    WOODCUTTER(type = CardType.ACTION, cost = 3, addCoins = 2, addBuys = 1),
//    FEAST(type = CardType.ACTION, cost = 4, effectList = listOf(::feastEffect)),
//    SPY(type = CardType.ACTION, cost = 4, addActions = 1, addCards = 1, effectList = listOf(::spyEffect)), // TODO: attack
//    THIEF(type = CardType.ACTION, cost = 4, effectList = listOf(::thiefEffect)), // TODO: attack
//    ADVENTURER(type = CardType.ACTION, cost = 6, effectList = listOf(::adventurerEffect)),

    COPPER(type = CardType.TREASURE, cost = 0, addCoins = 1),
    SILVER(type = CardType.TREASURE, cost = 3, addCoins = 2),
    GOLD(type = CardType.TREASURE, cost = 6, addCoins = 3),

    // TODO: it might be nice to make the Gardens card handle its own effect
    //       rather than be specifically handled by the engine
    //       -- maybe vp can be a function
    GARDENS(type = CardType.OTHER, cost = 4),

    ESTATE(type = CardType.OTHER, cost = 2, vp = 1),
    DUCHY(type = CardType.OTHER, cost = 5, vp = 3),
    PROVINCE(type = CardType.OTHER, cost = 8, vp = 6),

    CURSE(type = CardType.OTHER, cost = 0, vp = -1);
}

fun <T> MutableList<T>.prepend(element: T) {
    add(0, element)
}

fun MutableList<Card>.removeCard(card: Card): Card? {
    return remove(card).takeIf { it }?.let { card }
}

fun MutableList<Card>.addCard(card: Card): Card {
    return add(card).let { card }
}

fun MutableList<Card>.prependCard(card: Card): Card {
    return prepend(card).let { card }
}