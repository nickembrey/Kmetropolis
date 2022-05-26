package engine

import GameState

fun run(state: GameState) {
    while(!state.gameOver) {
        state.next()
    }
}