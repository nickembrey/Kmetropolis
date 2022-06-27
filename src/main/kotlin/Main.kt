import engine.GameState
import engine.Player
import engine.PlayerNumber
import policies.MCTSPolicy
import policies.badWitchPolicy
import policies.jansen_tollisen.UCTorigPolicy

// TODO: directory for all policies so imports aren't constantly changing

fun main(args: Array<String>) {

    var playerOneWins = 0
    var playerTwoWins = 0

    // TODO: names should always be just the policy name.
    val playerOneName = "Bad Witch 1"
    val playerTwoName = "Bad Witch 2"

    var totalPlayouts = 0
    var totalDecisions = 0

    val totalGames = 1000
    val games: MutableList<GameState> = mutableListOf()

    for(i in 1..totalGames) {
        val playerOne = Player(playerOneName, PlayerNumber.PlayerOne, ::badWitchPolicy)
        val playerTwo = Player(playerTwoName, PlayerNumber.PlayerTwo, ::badWitchPolicy)
        val gameState = GameState(playerOne, playerTwo, verbose=true)
        gameState.initialize()
        while(!gameState.gameOver) {
            gameState.makeNextDecision(gameState.choicePlayer.policy)
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

    if(totalPlayouts > 0) {
        print("\nAverage number of playouts per decision: " + (totalPlayouts / totalDecisions) + "\n")
    } else {
        print("\nNo playouts")
    }


    print("\n$playerOneName: $playerOneWins\n")
    print("\n$playerTwoName: $playerTwoWins\n\n")
    val ties = totalGames - playerOneWins - playerTwoWins
    print("\nTies: $ties\n\n")
}