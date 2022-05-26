import engine.player.*

fun main(args: Array<String>) {

    var playerOneWins = 0
    var playerTwoWins = 0

    val playerOneName = "Bad Witch"
    val playerTwoName = "MCTS Player"

    for(i in 1..100) {
        val playerOne = Player(playerOneName, badWitchPolicy)
        val playerTwo = Player(playerTwoName, MCTSPolicy)
        val gameState = GameState(playerOne, playerTwo)
        gameState.initialize()
        while(!gameState.gameOver) {
            gameState.next()
        }
        if(gameState.playerOne.vp > gameState.playerTwo.vp) {
            playerOneWins += 1
        } else if(gameState.playerTwo.vp > gameState.playerOne.vp) {
            playerTwoWins += 1
        }
    }

    print("\n$playerOneName: $playerOneWins\n")
    print("\n$playerTwoName: $playerTwoWins\n\n")
}