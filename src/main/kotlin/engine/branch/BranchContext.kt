package engine.branch

import engine.*
import engine.ContextBearer

enum class BranchContext: GameEvent, ContextBearer, GamePropertyValue {
    GAME_OVER,
    DRAW,
    CHOOSE_ACTION,
    CHOOSE_TREASURE,
    CHOOSE_BUY,
    CHAPEL,
    WORKSHOP,
    MILITIA,
    REMODEL_TRASH,
    REMODEL_GAIN,
    ANY,
    NONE;

    companion object {
        val gameOver = listOf(SpecialBranchSelection.GAME_OVER)
        val skipList = listOf(SpecialBranchSelection.SKIP)
    }

    override val event = this
    override val context = this

    fun toOptions(state: GameState): Collection<BranchSelection> {

        return when (this) {
            DRAW -> when(state.currentPlayer.deckSize) {
                0 -> skipList
                else -> state.currentPlayer.getDrawPossibilities()
            }
            GAME_OVER -> gameOver
            CHOOSE_BUY -> state.buyMenu
            CHAPEL -> state.currentPlayer.hand
            WORKSHOP -> state.workshopMenu
            MILITIA -> state.currentPlayer.hand
            REMODEL_TRASH -> state.currentPlayer.hand.ifEmpty { skipList }
            REMODEL_GAIN -> state.remodelMenu.ifEmpty { skipList }
            CHOOSE_ACTION -> state.currentPlayer.actionMenu
            CHOOSE_TREASURE -> state.currentPlayer.treasureMenu
            ANY, NONE -> throw IllegalStateException()
        }
    }
}