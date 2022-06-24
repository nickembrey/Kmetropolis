package engine
fun run(state: GameState) {
    while(!state.gameOver) {
        state.next()
    }
}