package engine

import engine.card.CardType
import engine.branch.BranchContext
import engine.operation.property.SetFromPropertyOperation
import engine.operation.property.SetToPropertyOperation
import engine.operation.stack.StackConditionalOperation
import engine.operation.stack.StackRepeatedOperation
import engine.operation.stack.StackSimpleOperation
import engine.operation.stack.game.GameCompoundOperation
import engine.operation.state.game.GameSimpleOperation

// TODO: just make these all sequences?
// TODO: then the list will go onto the stack in GameState and get processed in order

// TODO: these are a bit repetitious

enum class GamePhase(val events: List<GameEvent>): GamePropertyValue {
    NOT_STARTED(
        listOf(GameCompoundOperation.NEXT_PHASE, StackSimpleOperation.ADD_PHASE_OPERATIONS).reversed()
    ),
    START_GAME(
        List(5) { BranchContext.DRAW } // TODO: do we need the shuffles anymore?
            .plus(listOf(
                GameSimpleOperation.SWITCH_PLAYER,
                SetToPropertyOperation.SET_BUYS(1),
                SetToPropertyOperation.SET_COINS(0)
            ))
            .plus(List(5) { BranchContext.DRAW })
            .plus(listOf(
                GameSimpleOperation.SWITCH_PLAYER,
                SetToPropertyOperation.SET_BUYS(1),
                SetToPropertyOperation.SET_COINS(0))
            )
            .plus(listOf(GameCompoundOperation.NEXT_PHASE, StackSimpleOperation.ADD_PHASE_OPERATIONS))
            .reversed()
    ),
    ACTION(
        listOf(
            BranchContext.CHOOSE_ACTION,
            GameCompoundOperation.NEXT_PHASE, StackSimpleOperation.ADD_PHASE_OPERATIONS)
            .reversed()
    ),
    TREASURE(
        listOf(
            BranchContext.CHOOSE_TREASURE,
            GameCompoundOperation.NEXT_PHASE, StackSimpleOperation.ADD_PHASE_OPERATIONS)
            .reversed() // TODO: remove all these?
    ),
    BUY(
        listOf<GameEvent>(
            StackRepeatedOperation(
                repeatFn = { it.currentPlayer.buys },
                repeatedEvent = BranchContext.CHOOSE_BUY,
                context = BranchContext.CHOOSE_BUY
            ),
            GameCompoundOperation.NEXT_PHASE, StackSimpleOperation.ADD_PHASE_OPERATIONS)
            .reversed()
    ),
    END_TURN(
        listOf<GameEvent>( // TODO: these should be organized under play
            StackSimpleOperation.CLEANUP_ALL,
            StackSimpleOperation.DISCARD_ALL,
            StackSimpleOperation.CHECK_GAME_OVER)
            .plus(List(5) { BranchContext.DRAW })
            .plus(listOf(GameSimpleOperation.INCREMENT_TURNS,
                SetToPropertyOperation.SET_BUYS(1),
                SetToPropertyOperation.SET_COINS(0),
                GameSimpleOperation.SWITCH_PLAYER))
            .plus(listOf(GameCompoundOperation.NEXT_PHASE, StackSimpleOperation.ADD_PHASE_OPERATIONS))
            .reversed()
    ),
    END_GAME( listOf() );

    val next: GamePhase
        get() = when(this) {
            NOT_STARTED -> START_GAME
            START_GAME -> ACTION
            ACTION -> TREASURE
            TREASURE -> BUY
            BUY -> END_TURN
            END_TURN -> ACTION
            END_GAME -> throw IllegalCallerException("Can't call next on END_GAME phase!")
        }
}