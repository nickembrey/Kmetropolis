package engine

import engine.branch.Branch
import engine.branch.BranchContext
import engine.operation.property.SetToPropertyOperation
import engine.operation.stack.StackSimpleOperation
import engine.operation.stack.game.GameCompoundOperation
import engine.operation.state.game.GameSimpleOperation

// TODO: just make these all sequences?
// TODO: then the list will go onto the stack in GameState and get processed in order

// TODO: these are a bit repetitious


// TODO: make these functions or something simpler
enum class GamePhase(): GamePropertyValue {
    NOT_STARTED,
    START_GAME,
    ACTION,
    TREASURE,
    BUY,
    END_TURN,
    END_GAME;

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