package engine

enum class PlayerNumber(val value: String): PlayerTag {
    PLAYER_ONE("Player 1"),
    PLAYER_TWO("Player 2");

    // TODO: I think there are places where this would be useful but is not being used
    override fun getPlayer(state: GameState): Player = when(this) {
        PLAYER_ONE -> state.playerOne
        PLAYER_TWO -> state.playerTwo
    }

}