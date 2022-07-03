import engine.DominionLogger
import engine.GameState
import engine.Player
import engine.PlayerNumber
import kingdoms.jansenTollisenBoard
import policies.jansen_tollisen.UCTorigPolicy
import policies.singleWitchPolicy

import java.io.File

fun main(args: Array<String>) {
    // TODO: tuck all this stuff away in a Simulation class

    val policies = listOf(UCTorigPolicy, singleWitchPolicy)

    val logger = DominionLogger(
        logDirectory = File("/Users/nickembrey/dev/KDominion/log"),
        players = policies.map { it.name }
    )

    val totalGames = 25
    val games: MutableList<GameState> = mutableListOf()

    for(i in 1..totalGames) {
        val playerOne = Player(PlayerNumber.PlayerOne, UCTorigPolicy)
        val playerTwo = Player(PlayerNumber.PlayerTwo, singleWitchPolicy)
        val gameState = GameState(playerOne, playerTwo, board = jansenTollisenBoard, logger = logger)
        gameState.initialize()
        while(!gameState.gameOver) {
            gameState.makeNextCardDecision()
        }

        logger.recordGame(gameState)

        games.add(gameState)
    }

    logger.recordSimulation()
}