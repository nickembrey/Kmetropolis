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
    MINE_TRASH,
    MINE_GAIN,
    SENTRY_TRASH,
    SENTRY_DISCARD,
    SENTRY_TOPDECK,
    ARTISAN_GAIN,
    ARTISAN_TOPDECK,
    ANY,
    NONE;

    override val context = this
}