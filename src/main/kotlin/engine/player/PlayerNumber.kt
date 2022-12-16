package engine.player

import engine.GamePropertyValue
import engine.GameState

enum class PlayerNumber(val number: Int, val str: String): PlayerTag, GamePropertyValue {
    PLAYER_ONE(0, "Player 1"),
    PLAYER_TWO(1, "Player 2");

    companion object {
        fun fromInt(int: Int) =
            when(int) {
                0 -> PLAYER_ONE
                1 -> PLAYER_TWO
                else -> throw IllegalArgumentException()
            }
    }

    val next
        get() = when (this) {
            PLAYER_ONE -> PLAYER_TWO
            PLAYER_TWO -> PLAYER_ONE
        }

    override fun getPlayer(state: GameState): Player = when(this) {
        PLAYER_ONE -> state.players[0]
        PLAYER_TWO -> state.players[1]
    }

    override fun getOpponent(state: GameState): Player = when(this) {
        PLAYER_ONE -> state.players[1]
        PLAYER_TWO -> state.players[0]
    }

}