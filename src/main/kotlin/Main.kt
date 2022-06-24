import engine.GameState
import engine.Player
import policies.MCTSPolicy
import policies.badWitchPolicy

fun main(args: Array<String>) {

    var playerOneWins = 0
    var playerTwoWins = 0

    val playerOneName = "Bad Witch"
    val playerTwoName = "MCTS Player"

    var totalPlayouts = 0
    var totalDecisions = 0

    val totalGames = 1
    val games: MutableList<GameState> = mutableListOf()

    for(i in 1..totalGames) {
        val playerOne = Player(playerOneName, badWitchPolicy)
        val playerTwo = Player(playerTwoName, MCTSPolicy)
        val gameState = GameState(playerOne, playerTwo, verbose=true)
        gameState.initialize()
        while(!gameState.gameOver) {
            gameState.next()
        }
        if(gameState.playerOne.vp > gameState.playerTwo.vp) {
            playerOneWins += 1
        } else if(gameState.playerTwo.vp > gameState.playerOne.vp) {
            playerTwoWins += 1
        }
        totalPlayouts += gameState.logger.playouts
        totalDecisions += gameState.logger.decisions
        games.add(gameState)
    }

    print("\nAverage number of playouts per decision: " + (totalPlayouts / totalDecisions) + "\n")

    print("\n$playerOneName: $playerOneWins\n")
    print("\n$playerTwoName: $playerTwoWins\n\n")
    val ties = totalGames - playerOneWins - playerTwoWins
    print("\nTies: $ties\n\n")
}