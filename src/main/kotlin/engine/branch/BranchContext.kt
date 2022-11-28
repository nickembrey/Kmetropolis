package engine.branch

import engine.ContextBearer

enum class BranchContext: ContextBearer {
    ATTACK,
    GAME_OVER,
    DRAW,
    CHOOSE_ACTION,
    CHOOSE_TREASURE,
    CHOOSE_BUY,
    CELLAR,
    CHAPEL,
    HARBINGER,
    VASSAL,
    WORKSHOP,
    BUREAUCRAT,
    MILITIA,
    REMODEL_TRASH,
    REMODEL_GAIN,
    THRONE_ROOM,
    BANDIT,
    LIBRARY,
    ANY,
    NONE;

    override val context = this
}