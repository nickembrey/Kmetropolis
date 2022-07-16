import stats.DominionLogger
import engine.GameState
import kingdoms.jansenTollisenBoard
import policies.jansen_tollisen.SingleWitchPolicy
import policies.parallelized.UCTorigParallelPolicy

import java.io.File

fun main(args: Array<String>) {

    val policies = Pair(UCTorigParallelPolicy(), SingleWitchPolicy())

    val logger = DominionLogger(
        logDirectory = File("/Users/nick/dev/dominion/KDominion/log")
    )

    val totalGames = 10
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

    policies.first.threadPool.shutdown()
    logger.recordSimulation()
}