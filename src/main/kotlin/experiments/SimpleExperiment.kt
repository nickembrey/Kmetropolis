package experiments

import engine.GameResult
import engine.GameState
import engine.branch.BranchContext
import engine.branch.BranchSelection
import engine.performance.util.CardCountMap
import engine.player.PlayerNumber
import kingdoms.jansenTollisenBoard
import logger
import policies.Policy
import policies.PolicyName
import util.input.setStartingPolicy

class SimpleExperiment(
    private val policy1: Policy,
    private val policy2: Policy,
    private val board: CardCountMap = jansenTollisenBoard,
    private val chooseStartingPolicy: Boolean = false
): Experiment {

    override fun run(times: Int): ExperimentResult {

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
                board = board,
                maxTurns = 999,
                log = true,
                startingPolicy = if(chooseStartingPolicy) setStartingPolicy(policy1, policy2) else null
            )
            logger.initGame(gameState)
            while(!gameState.gameOver) {
                gameState.processEvent(gameState.getNextEvent())
            }

            logger.recordGame(gameState)

            gameLogs.add(gameState.branchSelectionHistory)

            gameResults[gameState.players[0].policy.name]!!.add(PlayerGameSummary(
                playerNumber = gameState.players[0].playerNumber,
                deck = gameState.players[0].allCards.toList(),
                result = when {
                    gameState.players[0].vp > gameState.players[1].vp -> GameResult.WIN
                    gameState.players[0].vp < gameState.players[1].vp -> GameResult.LOSE
                    gameState.players[0].vp == gameState.players[1].vp -> GameResult.TIE
                    else -> throw IllegalStateException()
                },
                vp = gameState.players[0].vp
            ))
            gameResults[gameState.players[1].policy.name]!!.add(PlayerGameSummary(
                playerNumber = gameState.players[1].playerNumber,
                deck = gameState.players[1].allCards.toList(),
                result = when {
                    gameState.players[1].vp > gameState.players[0].vp -> GameResult.WIN
                    gameState.players[1].vp < gameState.players[0].vp -> GameResult.LOSE
                    gameState.players[1].vp == gameState.players[0].vp -> GameResult.TIE
                    else -> throw IllegalStateException()
                },
                vp = gameState.players[1].vp
            ))
        }

        return ExperimentResult(
            settings = ExperimentSettings(
                policy1 = policy1.name,
                policy2 = policy2.name
            ),
            gameLogs = gameLogs,
            gameResults = gameResults
        )
    }

}