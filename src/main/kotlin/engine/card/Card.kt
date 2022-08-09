package engine.card

import engine.GameEvent
import engine.branch.BranchSelection
import engine.operation.property.ModifyPropertyOperation
import engine.operation.stack.player.PlayerCardOperation
import engine.operation.state.player.PlayerMoveCardOperation
import engine.operation.state.player.PlayerSimpleOperation
import util.memoize

// TODO: redo cards
enum class Card(
    val type: CardType,
    val cost: Int,
    val addActions: Int = 0,
    val addBuys: Int = 0,
    val addCards: Int = 0,
    val addCoins: Int = 0,
    val vp: Int = 0,
): BranchSelection, Comparable<Card> { // TODO: organize parameters

    CHAPEL(type = CardType.ACTION, cost = 2),
    VILLAGE(type = CardType.ACTION, cost = 3, addCards = 1, addActions = 2),
    WORKSHOP(type = CardType.ACTION, cost = 3),
    MILITIA(type = CardType.ACTION, cost = 4, addCoins = 2), // TODO: attack
    MONEYLENDER(type = CardType.ACTION, cost = 4),
    REMODEL(type = CardType.ACTION, cost = 4),
    SMITHY(type = CardType.ACTION, cost = 4, addCards = 3),
    FESTIVAL(type = CardType.ACTION, cost = 5, addCoins = 2, addActions = 2, addBuys = 1),
    LABORATORY(type = CardType.ACTION, cost = 5, addCards = 2, addActions = 1),
    MARKET(type = CardType.ACTION, cost = 5, addCards = 1, addActions = 1, addBuys = 1),
    WITCH(type = CardType.ACTION, cost = 5, addCards = 2), // TODO: figure out how to handle attacks and moat blocks
    WOODCUTTER(type = CardType.ACTION, cost = 3, addCoins = 2, addBuys = 1),

    COPPER(type = CardType.TREASURE, cost = 0, addCoins = 1),
    SILVER(type = CardType.TREASURE, cost = 3, addCoins = 2),
    GOLD(type = CardType.TREASURE, cost = 6, addCoins = 3),

    // TODO: it might be nice to make the Gardens card handle its own effect
    //       rather than be specifically handled by the engine
    GARDENS(type = CardType.VICTORY, cost = 4),

    ESTATE(type = CardType.VICTORY, cost = 2, vp = 1),
    DUCHY(type = CardType.VICTORY, cost = 5, vp = 3),
    PROVINCE(type = CardType.VICTORY, cost = 8, vp = 6),

    CURSE(type = CardType.CURSE, cost = 0, vp = -1);

    val effects: List<GameEvent>
        get() = when (this) {
            CHAPEL -> CardEffects.CHAPEL_EFFECT
            WORKSHOP -> CardEffects.WORKSHOP_EFFECT
            MILITIA -> CardEffects.MILITIA_EFFECT
            MONEYLENDER -> CardEffects.MONEYLENDER_EFFECT
            REMODEL -> CardEffects.REMODEL_EFFECT
            WITCH -> CardEffects.WITCH_EFFECT
            else -> listOf()
        }
}