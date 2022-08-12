package engine.branch

import engine.*
import engine.ContextBearer

enum class BranchContext: ContextBearer, GamePropertyValue {
    GAME_OVER,
    DRAW,
    CHOOSE_ACTION,
    CHOOSE_TREASURE,
    CHOOSE_BUYS,
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

    override val context = this
}