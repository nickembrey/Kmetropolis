package engine.performance

import engine.GameEvent
import engine.branch.BranchContext
import engine.card.Card
import engine.card.CardLocation
import engine.operation.Operation.Companion.NO_OP
import engine.operation.property.ModifyPropertyOperation
import engine.operation.property.SetFromPropertyOperation
import engine.operation.property.SetToPropertyOperation
import engine.operation.stack.StackConditionalOperation
import engine.operation.stack.StackMultipleOperation
import engine.operation.state.player.PlayerMoveCardOperation
import engine.operation.state.player.PlayerSimpleOperation
import engine.performance.util.CardEventMap


object CardEffectsMap: CardEventMap(Card.values().associateWith { NO_OP }) {

    init {
        Card.values().forEach { card ->
            backingArray[card.ordinal] = StackMultipleOperation(
                events =  card.effects + commonEffects(card),
                context = BranchContext.NONE
            )
        }
    }

    private fun commonEffects(card: Card): List<GameEvent> {
        return List(card.addActions) { BranchContext.CHOOSE_ACTION }
            .plus( listOf(
                StackConditionalOperation(
                    context = BranchContext.DRAW,
                    condition = { card.addCards > 0 },
                    conditionalEvent = StackMultipleOperation(
                        context = BranchContext.DRAW,
                        events = listOf(
                            StackMultipleOperation(
                                events = List(card.addCards) { BranchContext.DRAW },
                                context = BranchContext.NONE),
                        ) ) )))
            .let {
                if(card.addCoins > 0) {
                    it.plus(ModifyPropertyOperation.MODIFY_COINS(card.addCoins))
                } else {
                    it
                }
            }
            .let {
                if (card.addBuys > 0) {
                    it.plus(ModifyPropertyOperation.MODIFY_BUYS(card.addBuys))
                } else {
                    it
                }
            }.plus(
                PlayerMoveCardOperation(
                    card = card,
                    from = CardLocation.HAND,
                    to = CardLocation.IN_PLAY)
            )
    }






}