package experiments

import engine.GameResult
import engine.GameState
import engine.branch.BranchContext
import engine.branch.BranchSelection
import kingdoms.jansenTollisenBoard
import logger
import policies.Policy
import policies.PolicyName

class SimpleExperiment( // TODO: should take in an experimentSettings object
    private val policy1: Policy,
    private val policy2: Policy
): Experiment {

    override fun run(times: Int): ExperimentResult {

        // TODO: seems like a lot of this should be done for ALL experiments.. so move it up?

        val gameLogs: MutableList<List<Triple<PolicyName, BranchContext, BranchSelection>>> = mutableListOf()

        val gameResults: Map<PolicyName, MutableList<PlayerGameSummary>> = mapOf(
            policy1.name to mutableListOf(),
            policy2.name to mutableListOf()
        )

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
                gameState.processEvent(gameState.getNextEvent()) // TODO: just let us use the eventStack directly?
            }

            // TODO:
//            policy1.endGame()
//            policy2.endGame()
            logger.recordGame(gameState)

            gameLogs.add(gameState.branchSelectionHistory)

            // TODO: KISS
            gameResults[gameState.players[0].policy.name]!!.add(PlayerGameSummary( // TODO: !!
                playerNumber = gameState.players[0].playerNumber,
                deck = gameState.players[0].allCards,
                result = when {
                    gameState.players[0].vp > gameState.players[1].vp -> GameResult.WIN
                    gameState.players[0].vp < gameState.players[1].vp -> GameResult.LOSE
                    gameState.players[0].vp == gameState.players[1].vp -> GameResult.TIE
                    else -> throw IllegalStateException()
                },
                vp = gameState.players[0].vp
            ))
            gameResults[gameState.players[1].policy.name]!!.add(PlayerGameSummary( // TODO: !!
                playerNumber = gameState.players[1].playerNumber,
                deck = gameState.players[1].allCards,
                result = when {
                    gameState.players[1].vp > gameState.players[0].vp -> GameResult.WIN
                    gameState.players[1].vp < gameState.players[0].vp -> GameResult.LOSE
                    gameState.players[1].vp == gameState.players[0].vp -> GameResult.TIE
                    else -> throw IllegalStateException()
                },
                vp = gameState.players[1].vp
            ))
        }

        return ExperimentResult( // TODO: versioning for players
            settings = ExperimentSettings(
                policy1 = policy1.name,
                policy2 = policy2.name
            ),
            gameLogs = gameLogs,
            gameResults = gameResults
        )
    }

}