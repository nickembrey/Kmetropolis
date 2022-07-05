import engine.DominionLogger
import engine.GameState
import engine.Player
import engine.PlayerNumber
import kingdoms.jansenTollisenBoard
import policies.jansen_tollisen.UCTorigPolicy
import policies.jansen_tollisen.singleWitchPolicy

import java.io.File

fun main(args: Array<String>) {
    // TODO: tuck all this stuff away in a Simulation class

    val policies = Pair(UCTorigPolicy, singleWitchPolicy)

    val logger = DominionLogger(
        logDirectory = File("/Users/nickembrey/dev/KDominion/log")
    )

    val totalGames = 50
    val games: MutableList<GameState> = mutableListOf()

    for(i in 1..totalGames) {
        val gameState = GameState(
            policies = policies,
            board = jansenTollisenBoard,
            logger = logger
        )
        gameState.initialize()
        while(!gameState.gameOver) {
            gameState.makeNextCardDecision()
        }

        logger.recordGame(gameState)

        games.add(gameState)
    }

    logger.recordSimulation()
}