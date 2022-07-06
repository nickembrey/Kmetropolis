package engine

interface PlayerTag {
    fun getPlayer(state: GameState): Player
}