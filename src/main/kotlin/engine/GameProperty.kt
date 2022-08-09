package engine

import engine.operation.property.StateProperty

enum class GameProperty: StateProperty {
    PLAYER, PHASE, TURNS, CONTEXT, GAME_OVER;
}