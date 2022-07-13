package engine.player

import engine.GameState

interface PlayerTag {
    fun getPlayer(state: GameState): Player
}