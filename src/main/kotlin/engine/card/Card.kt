package engine.card

import engine.AttackEvent
import engine.DelayedGameOperation
import engine.GameState
import engine.SpecialGameEvent
import engine.operation.property.ModifyPropertyOperation
import engine.operation.stack.player.PlayerCardOperation

enum class Card(
    val type: CardType, // TODO: should allow more than one
    val cost: Int,
    val addActions: Int = 0,
    val addBuys: Int = 0,
    val addCards: Int = 0,
    val addCoins: Int = 0,
    val vp: Int = 0,
    val effect: (GameState) -> Unit = {}
): Comparable<Card> {

    CELLAR(type = CardType.ACTION, cost = 2, effect = {
        it.eventStack.push(engine.branch.Branch(engine.branch.BranchContext.CELLAR, selections = 1)) // TODO: only one?
    }),
    CHAPEL(type = CardType.ACTION, cost = 2, effect = {
        it.eventStack.push(engine.branch.Branch(engine.branch.BranchContext.CHAPEL, selections = 4))
    }),
    MOAT(type = CardType.ACTION, cost = 2),
    HARBINGER(type = CardType.ACTION, cost = 3, addActions = 1,addCards = 1, effect = {
        it.eventStack.push(engine.branch.Branch(engine.branch.BranchContext.HARBINGER, selections = 1))
    }),
    MERCHANT(type = CardType.ACTION, cost = 3, addCards = 1, addActions = 1),
    VASSAL(type = CardType.ACTION, cost = 3, effect = {
        val card = it.currentPlayer.sample(1).single()
        it.currentPlayer.discard(card)
        if(card.type == CardType.ACTION) {
            it.currentPlayer.vassalCard = card
            it.eventStack.push(engine.branch.Branch(engine.branch.BranchContext.VASSAL, selections = 1))
        }
    }),
    VILLAGE(type = CardType.ACTION, cost = 3, addCards = 1, addActions = 2),
    WORKSHOP(type = CardType.ACTION, cost = 3, effect = {
        it.eventStack.push(engine.branch.Branch(engine.branch.BranchContext.WORKSHOP))
    }),
    MILITIA(type = CardType.ACTION, cost = 4, addCoins = 2, effect = {
        it.eventStack.pushAll(
            listOf(
                SpecialGameEvent.SWITCH_PLAYER,
                engine.AttackEvent( attack = DelayedGameOperation { state ->
                    state.eventStack.push(
                        engine.branch.Branch(
                            engine.branch.BranchContext.MILITIA,
                            selections = state.currentPlayer.handCount - 3
                        )
                    )
                }),
                SpecialGameEvent.SWITCH_PLAYER
            ).reversed())
    }),
    MONEYLENDER(type = CardType.ACTION, cost = 4, effect = {
        if(!it.currentPlayer.visibleHand) {
            throw IllegalStateException()
        }
        if(it.currentPlayer.knownHand[COPPER] > 0) {
            it.processOperation(ModifyPropertyOperation.MODIFY_COINS(3))
            it.processOperation(PlayerCardOperation.TRASH(COPPER))
        }
    }),
    REMODEL(type = CardType.ACTION, cost = 4, effect = {
        it.eventStack.pushAll(
            listOf(
                engine.branch.Branch(context = engine.branch.BranchContext.REMODEL_TRASH),
                engine.branch.Branch(engine.branch.BranchContext.REMODEL_GAIN) // TODO: make sure skips if no remodel card
            ).reversed()

        )
    }),
    SMITHY(type = CardType.ACTION, cost = 4, addCards = 3),
    THRONE_ROOM(type = CardType.ACTION, cost = 4, effect = {
        it.eventStack.push(engine.branch.Branch(engine.branch.BranchContext.THRONE_ROOM, selections = 1))
    }),
    FESTIVAL(type = CardType.ACTION, cost = 5, addCoins = 2, addActions = 2, addBuys = 1),
    LABORATORY(type = CardType.ACTION, cost = 5, addCards = 2, addActions = 1),
    MARKET(type = CardType.ACTION, cost = 5, addCards = 1, addActions = 1, addBuys = 1),
    WITCH(type = CardType.ACTION, cost = 5, addCards = 2, effect = {
        it.eventStack.pushAll(
            listOf(
                SpecialGameEvent.SWITCH_PLAYER,
                AttackEvent(attack = DelayedGameOperation { state ->
                    if(state.board[CURSE] > 0) {
                        state.currentPlayer.gain(CURSE) // TODO: other player or current player?
                    }
                }),
                SpecialGameEvent.SWITCH_PLAYER
            ).reversed())
    }),
    WOODCUTTER(type = CardType.ACTION, cost = 3, addCoins = 2, addBuys = 1),

    COPPER(type = CardType.TREASURE, cost = 0, addCoins = 1),
    SILVER(type = CardType.TREASURE, cost = 3, addCoins = 2, effect = {
        it.currentPlayer.coins += it.currentPlayer.inPlay.toList()
            .filter { card -> card == MERCHANT }
            .size
    }),
    GOLD(type = CardType.TREASURE, cost = 6, addCoins = 3),

    GARDENS(type = CardType.VICTORY, cost = 4),

    ESTATE(type = CardType.VICTORY, cost = 2, vp = 1),
    DUCHY(type = CardType.VICTORY, cost = 5, vp = 3),
    PROVINCE(type = CardType.VICTORY, cost = 8, vp = 6),

    CURSE(type = CardType.CURSE, cost = 0, vp = -1);
}