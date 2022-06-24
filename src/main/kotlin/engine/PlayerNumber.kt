package engine

enum class PlayerNumber(val value: String) {
    PlayerOne("Player One"),
    PlayerTwo("Player Two");

    fun getPlayer(state: GameState): Player = when(this) {
        PlayerOne -> state.playerOne
        PlayerTwo -> state.playerTwo
    }

    fun getOpponent(state: GameState): Player = when(this) {
        PlayerOne -> state.playerTwo
        PlayerTwo -> state.playerOne
    }

}