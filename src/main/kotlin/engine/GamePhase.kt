package engine

import engine.branch.Branch
import engine.branch.BranchContext
import engine.operation.property.SetToPropertyOperation
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
        listOf(
                Branch(context = BranchContext.DRAW, selections = 5),
                GameSimpleOperation.SWITCH_PLAYER,
                SetToPropertyOperation.SET_BUYS(1),
                SetToPropertyOperation.SET_COINS(0),
                Branch(context = BranchContext.DRAW, selections = 5),
                GameSimpleOperation.SWITCH_PLAYER,
                SetToPropertyOperation.SET_BUYS(1),
                SetToPropertyOperation.SET_COINS(0),
                GameCompoundOperation.NEXT_PHASE,
                StackSimpleOperation.ADD_PHASE_OPERATIONS
            ).reversed()
    ),
    ACTION(
        listOf(
            Branch(BranchContext.CHOOSE_ACTION),
            GameCompoundOperation.NEXT_PHASE, StackSimpleOperation.ADD_PHASE_OPERATIONS)
            .reversed()
    ),
    TREASURE(
        listOf(
            Branch(BranchContext.CHOOSE_TREASURE),
            GameCompoundOperation.NEXT_PHASE, StackSimpleOperation.ADD_PHASE_OPERATIONS)
            .reversed() // TODO: remove all these?
    ),
    BUY(
        listOf(
            Branch(BranchContext.CHOOSE_BUYS),
            GameCompoundOperation.NEXT_PHASE, StackSimpleOperation.ADD_PHASE_OPERATIONS)
            .reversed()
    ),
    END_TURN(
        listOf(
            StackSimpleOperation.CLEANUP_ALL,
            StackSimpleOperation.DISCARD_ALL,
            StackSimpleOperation.CHECK_GAME_OVER,
            Branch(context = BranchContext.DRAW, selections = 5),
            GameSimpleOperation.INCREMENT_TURNS,
            SetToPropertyOperation.SET_BUYS(1),
            SetToPropertyOperation.SET_COINS(0),
            GameSimpleOperation.SWITCH_PLAYER,
            GameCompoundOperation.NEXT_PHASE,
            StackSimpleOperation.ADD_PHASE_OPERATIONS
        ).reversed()
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