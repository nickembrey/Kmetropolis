package engine.player

import engine.GameState

enum class PlayerNumber(val value: String): PlayerTag {
    PLAYER_ONE("Player 1"),
    PLAYER_TWO("Player 2");

    // TODO: I think there are places where this would be useful but is not being used
    override fun getPlayer(state: GameState): Player = when(this) {
        PLAYER_ONE -> state.playerOne
        PLAYER_TWO -> state.playerTwo
    }

    override fun getOpponent(state: GameState): Player = when(this) {
        PLAYER_ONE -> state.playerTwo
        PLAYER_TWO -> state.playerOne
    }

}