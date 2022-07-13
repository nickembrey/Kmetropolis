package engine.player

import engine.GameState

enum class PlayerRole(val value: String): PlayerTag {
    CURRENT_PLAYER("Current player"),
    OTHER_PLAYER("Other player");

    override fun getPlayer(state: GameState): Player = when(this) {
        CURRENT_PLAYER -> state.currentPlayer
        OTHER_PLAYER -> state.otherPlayer
    }
}