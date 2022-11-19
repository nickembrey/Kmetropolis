package engine.branch

import engine.ContextBearer

enum class BranchContext: ContextBearer {
    GAME_OVER,
    DRAW,
    CHOOSE_ACTION,
    CHOOSE_TREASURE,
    CHOOSE_BUY,
    CELLAR,
    CHAPEL,
    HARBINGER,
    WORKSHOP,
    MILITIA,
    REMODEL_TRASH,
    REMODEL_GAIN,
    ANY,
    NONE;

    override val context = this
}