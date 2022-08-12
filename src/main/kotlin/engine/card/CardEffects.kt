package engine.card

import engine.GameEvent
import engine.branch.Branch
import engine.branch.BranchContext
import engine.operation.property.ModifyPropertyOperation
import engine.operation.property.ReadPropertyOperation
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
        listOf(Branch(
            BranchContext.CHAPEL, selections = 4
        ))
    ),
    WORKSHOP_EFFECT(
        listOf(Branch(BranchContext.WORKSHOP))
    ),
    MILITIA_EFFECT(
        listOf(
            GameSimpleOperation.SWITCH_PLAYER,
            ReadPropertyOperation(
                readFn = { it.currentPlayer.hand.size },
                useFn = { SetToPropertyOperation(
                    target = engine.GameProperty.CONTEXT,
                    Branch(engine.branch.BranchContext.MILITIA, selections = it - 3)
                )}
            ),
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
            Branch(BranchContext.REMODEL_TRASH),
            StackConditionalOperation(
                conditionalEvent = Branch(BranchContext.REMODEL_GAIN),
                condition = { it.currentPlayer.remodelCard != null },
                context = BranchContext.NONE
            )
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