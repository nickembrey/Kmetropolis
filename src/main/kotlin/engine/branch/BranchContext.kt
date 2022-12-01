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
    VASSAL_DISCARD,
    VASSAL_PLAY,
    WORKSHOP,
    BUREAUCRAT,
    MILITIA,
    POACHER,
    REMODEL_TRASH,
    REMODEL_GAIN,
    THRONE_ROOM,
    BANDIT,
    LIBRARY_IDENTIFY,
    LIBRARY_DRAW,
    MINE_TRASH,
    MINE_GAIN,
    SENTRY_IDENTIFY,
    SENTRY_TRASH,
    SENTRY_DISCARD,
    SENTRY_TOPDECK,
    ARTISAN_GAIN,
    ARTISAN_TOPDECK,
    ANY,
    NONE;

    override val context = this
}