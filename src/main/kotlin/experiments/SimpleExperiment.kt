package experiments

import engine.GameState
import kingdoms.jansenTollisenBoard
import logger
import policies.Policy

class SimpleExperiment(private val policy1: Policy, private val policy2: Policy): Experiment {

    override fun run(times: Int) {

        val games: MutableList<GameState> = mutableListOf()

        logger.initSimulation()
        for(i in 1..times) {

            println("Starting game $i")
            println("")

            val gameState = GameState.new(
                policy1 = policy1,
                policy2 = policy2,
                board = jansenTollisenBoard,
                maxTurns = 999,
                log = true
            )
            logger.initGame(gameState)
            while(!gameState.gameOver) {
                gameState.processNextBranch()
            }

            policy1.endGame()
            policy2.endGame()
            logger.recordGame(gameState)

            games.add(gameState)
        }

        // TODO:
        policy1.shutdown()
        policy2.shutdown()
        logger.recordSimulation()
    }

}