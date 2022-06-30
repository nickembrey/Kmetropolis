import engine.DominionLogger
import engine.GameState
import engine.Player
import engine.PlayerNumber
import policies.badWitchPolicy
import java.io.File

// TODO: directory for all policies so imports aren't constantly changing

fun main(args: Array<String>) {

    // TODO: names should always be just the policy name.
    val playerOneName = "Bad Witch 1"
    val playerTwoName = "Bad Witch 2"

    val logger = DominionLogger(
        File("/Users/nickembrey/dev/KDominion/log"), listOf(playerOneName, playerTwoName))

    val totalGames = 10
    val games: MutableList<GameState> = mutableListOf()

    for(i in 1..totalGames) {
        val playerOne = Player(playerOneName, PlayerNumber.PlayerOne, ::badWitchPolicy)
        val playerTwo = Player(playerTwoName, PlayerNumber.PlayerTwo, ::badWitchPolicy)
        val gameState = GameState(playerOne, playerTwo, logger = logger)
        gameState.initialize()
        while(!gameState.gameOver) {
            gameState.choicePlayer.makeNextCardDecision(gameState)
        }

        logger.recordGame(gameState)

        games.add(gameState)
    }

    logger.recordSimulation()
}