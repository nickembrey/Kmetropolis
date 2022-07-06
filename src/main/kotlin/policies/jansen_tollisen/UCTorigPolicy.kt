package policies.jansen_tollisen

import engine.*
import mcts.MCTSTreeNode
import policies.Policy
import policies.PolicyName
import policies.rollout.jansen_tollisen.epsilonHeuristicGreedyPolicy
import kotlin.math.ln
import kotlin.math.sqrt

object UCTorigPolicy : Policy {
    override val name = PolicyName("UCTorigPolicy")
    override fun policy(
        state: GameState,
        choices: CardChoices
    ): Card? { // TODO: we can get the choices from the state ourselves

        state.logger?.startDecision()

        // must play plus actions first (MPPAF)
        if (state.context == ChoiceContext.ACTION) {
            val addActionsCard = choices.filterNotNull().firstOrNull { it.addActions > 0 }   // TODO:
            if (addActionsCard != null) {
                return addActionsCard
            }
        } else if (state.context == ChoiceContext.TREASURE) {
            return choices[0]
        }

        // TODO: pull out into settings either at Policy or Simulation level or both

        // TODO: need a settings class

        val cParameter = 0.7
        state.logger?.recordSimulationOptions(cParameter)

        val root = MCTSTreeNode(
            parent = null,
            card = null,
            playerNumber = state.choicePlayer.playerNumber,
            choiceContext = state.context
        )

        // TODO: remove subfunctions from here and in MCTS policy
        // Note that toMutableList is used below to create copies of the current state.
        // TODO: this could be a bottleneck? think about it.
        // TODO: epsilonHeuristicGreedyPolicy

        fun rollout(simState: GameState): Map<PlayerNumber, Double> {

            while (!simState.gameOver) {
                simState.makeNextCardDecision()
            }

            val playerOneVp = simState.playerOne.vp
            val playerTwoVp = simState.playerTwo.vp

            val playerOneScore = (if (playerOneVp > playerTwoVp) 1.0 else 0.0) +
                    (playerOneVp - playerTwoVp).toDouble() / 100.0
            val playerTwoScore = (if (playerTwoVp > playerOneVp) 1.0 else 0.0) +
                    (playerTwoVp - playerOneVp).toDouble() / 100.0

            // TODO: 100 is a magic number
            return mapOf(
                PlayerNumber.PLAYER_ONE to playerOneScore,
                PlayerNumber.PLAYER_TWO to playerTwoScore
            )
        }

        fun forward(node: MCTSTreeNode, simState: GameState, simChoices: CardChoices) {

            if (node.children.isNotEmpty()) {
                val index =
                    if (node.children.size == 1) {
                        0 // TODO: this should never happen, right?
                    } else if (node.children.any { it.simulations == 0 }) { // i.e., each child has not had a simulation
                        node.children.indexOfFirst { it.simulations == 0 }
                    } else {
                        node.children.map {
                            (it.score / it.simulations) +
                                    (cParameter * sqrt(ln(node.simulations.toDouble()) / it.simulations))
                        }.let { values ->
                            values.indices.maxByOrNull { values[it] }!!
                        }
                    }

                simChoices[index].let {
                    simState.makeCardDecision(it)
                }

                // TODO: wrap this up in a method
                var nextChoices = simState.context.getCardChoices(simState.choicePlayer, simState.board)
                while (true) {
                    if (nextChoices.size == 1) {
                        simState.makeCardDecision(nextChoices[0])
                    } else when (simState.context) {
                        ChoiceContext.ACTION -> { // must play plus actions first (MPPAF)
                            nextChoices.filterNotNull().firstOrNull { it.addActions > 0 }?.let {
                                simState.makeCardDecision(it)
                            } ?: break
                        }
                        ChoiceContext.TREASURE -> simState.makeCardDecision(nextChoices[0])
                        else -> break
                    }
                    nextChoices = simState.context.getCardChoices(simState.choicePlayer, simState.board)
                }

                forward(node.children[index], simState, nextChoices)
            } else {

                node.simulations = 1
                node.children = simChoices.map {
                    MCTSTreeNode(
                        parent = node,
                        playerNumber = simState.choicePlayer.playerNumber,
                        card = it,
                        choiceContext = simState.context
                    )
                }

                val rolloutResults = rollout(simState)
                node.score = rolloutResults[node.playerNumber!!]!!

                // backpropagation
                var current = node.parent
                while (current != null) {
                    current.score += rolloutResults[current.playerNumber]!!
                    current.simulations += 1
                    current = current.parent
                }
            }
        }


        val iterations = 100

        val shuffledState = state.copy(
            newPolicies = Pair(epsilonHeuristicGreedyPolicy, epsilonHeuristicGreedyPolicy),
            newTrueShuffle = false,
            newLogger = null,
            newMaxTurns = 40, // TODO:
            newPoliciesInOrder = true,
            obfuscateUnseen = true
        )

        for (it in 1..iterations) {
            state.logger?.startSimulation()
            forward(
                node = root,
                simState = shuffledState.copy(),
                simChoices = choices
            )
            state.logger?.endSimulation()
        }

        val simulations: List<Int> = root.children.map { it.simulations }
        val index = simulations.indices.maxByOrNull { simulations[it] }!!
        return choices[index].also { state.logger?.endDecision() }

    }

}