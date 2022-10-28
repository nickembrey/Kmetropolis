package engine

import engine.operation.property.StateProperty

enum class GameProperty: StateProperty {
    PLAYER, PHASE, TURNS, GAME_OVER;
}