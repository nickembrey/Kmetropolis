package engine.card

import engine.GameEvent
import engine.branch.BranchContext
import engine.operation.property.ModifyPropertyOperation
import engine.operation.stack.StackConditionalOperation
import engine.operation.stack.StackRepeatedOperation
import engine.operation.property.SetFromPropertyOperation
import engine.operation.property.SetToPropertyOperation
import engine.operation.stack.StackMultipleOperation
import engine.operation.stack.player.PlayerCardOperation
import engine.operation.state.game.GameSimpleOperation


// TODO: try to put these back in Card
enum class CardEffects(
    private val operations: List<GameEvent>
): List<GameEvent> by operations {

    CHAPEL_EFFECT(
        List(4) { BranchContext.CHAPEL }
    ),
    WORKSHOP_EFFECT(
        listOf(BranchContext.WORKSHOP)
    ),
    MILITIA_EFFECT(
        listOf(
            GameSimpleOperation.SWITCH_PLAYER,
            StackRepeatedOperation(
                repeatedEvent = BranchContext.MILITIA,
                repeatFn = { it.currentPlayer.hand.size - 3 },
                context = BranchContext.NONE),
            GameSimpleOperation.SWITCH_PLAYER
        )
    ),
    MONEYLENDER_EFFECT(
        listOf(StackConditionalOperation(
            condition = { it.currentPlayer.hand.contains(Card.COPPER) },
            conditionalEvent = StackMultipleOperation(
                context = BranchContext.NONE,
                events = listOf(
                    ModifyPropertyOperation.MODIFY_COINS(3),
                    PlayerCardOperation.TRASH(Card.COPPER),
                ),
            ),
            context = BranchContext.NONE)
        )
    ),
    REMODEL_EFFECT(
        listOf(
            BranchContext.REMODEL_TRASH, StackConditionalOperation(
            conditionalEvent = BranchContext.REMODEL_GAIN,
            condition = { it.currentPlayer.remodelCard != null },
            context = BranchContext.NONE)
        )
    ),
    WITCH_EFFECT(
        listOf(
            GameSimpleOperation.SWITCH_PLAYER,
            StackConditionalOperation(
                condition = { it.board[Card.CURSE]!! > 0 },
                conditionalEvent = PlayerCardOperation.GAIN(Card.CURSE),
                context = BranchContext.NONE
            ),
            GameSimpleOperation.SWITCH_PLAYER)
    )
}