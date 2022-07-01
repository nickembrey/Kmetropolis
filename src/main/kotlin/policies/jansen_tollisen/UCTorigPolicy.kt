package policies.jansen_tollisen

import engine.*
import engine.Player
import mcts.MCTSTreeNode
import policies.policy.epsilonHeuristicGreedyPolicy
import kotlin.math.ln

fun _UCTorigPolicy(
    state: GameState,
    choices: CardChoices
): Card? {

    // must play plus actions first (MPPAF)
    if(state.context == ChoiceContext.ACTION) {
        val addActionsCard = choices.filterNotNull().firstOrNull { it.addActions > 0 }   // TODO:
        if (addActionsCard != null) {
            return addActionsCard
        }
    } else if(state.context == ChoiceContext.TREASURE) {
        return choices[0]
    }

    // TODO: pull out into settings either at Policy or Simulation level or both

    // TODO: need a settings class
    val seconds = 2.0
    val cParameter = 0.7

//    state.logger?.recordSimulationOptions(seconds, cParameter) // TODO

    val root = MCTSTreeNode(
        parent = null,
        card = null,
        playerNumber = state.choicePlayer.playerNumber,
        choiceContext = state.context
    )

    // TODO: remove subfunctions from here and in MCTS policy
    // Note that toMutableList is used below to create copies of the current state.
    // TODO: this could be a bottleneck? think about it.
    fun getNewState(currentState: GameState, shuffle: Boolean = false): GameState {
        val playerOne = Player(
            "Player One",
            PlayerNumber.PlayerOne,
            epsilonHeuristicGreedyPolicy,
            currentState.playerOne.deck.toMutableList().also { if(shuffle) it.shuffle() }, // TODO:
            currentState.playerOne.hand.toMutableList(),
            currentState.playerOne.inPlay.toMutableList(),
            currentState.playerOne.discard.toMutableList()
        ).apply { remodelCard = currentState.playerOne.remodelCard }
        val playerTwo = Player(
            "Player Two",
            PlayerNumber.PlayerTwo,
            epsilonHeuristicGreedyPolicy,
            currentState.playerTwo.deck.toMutableList().also { if(shuffle) it.shuffle() }, // TODO:
            currentState.playerTwo.hand.toMutableList(),
            currentState.playerTwo.inPlay.toMutableList(),
            currentState.playerTwo.discard.toMutableList()
        ).apply { remodelCard = currentState.playerOne.remodelCard }
        return GameState(
            playerOne,
            playerTwo,
            currentState.board.toMutableMap(),
            currentState.turns,
            currentState.context,
            trueShuffle = false,
            maxTurns = 40
        ).apply { currentPlayer = when(currentState.currentPlayer.playerNumber) {
            PlayerNumber.PlayerOne -> playerOne
            PlayerNumber.PlayerTwo -> playerTwo
        } }
    }

    fun rollout(simState: GameState): Map<PlayerNumber, Double> {

        while(!simState.gameOver) {
            simState.choicePlayer.makeNextCardDecision(simState, epsilonHeuristicGreedyPolicy)
        }

        val playerOneVp = simState.playerOne.vp
        val playerTwoVp = simState.playerTwo.vp


        val playerOneScore = ( if (playerOneVp > playerTwoVp) 1.0 else 0.0 ) +
                (simState.playerOne.vp - simState.playerTwo.vp).toDouble() / 100.0
        val playerTwoScore = ( if (playerTwoVp > playerOneVp) 1.0 else 0.0 ) +
                (simState.playerTwo.vp - simState.playerOne.vp).toDouble() / 100.0

        // TODO: 100 is a magic number
        return mapOf(
            PlayerNumber.PlayerOne to playerOneScore,
            PlayerNumber.PlayerTwo to playerTwoScore
        )
    }

    fun forward(node: MCTSTreeNode, simState: GameState, simChoices: CardChoices) {

        if(node.children.isNotEmpty()) {
            val index =
                if (node.children.size == 1) {
                    0 // TODO: this should never happen, right?
                } else if (node.children.any { it.simulations == 0 }) { // i.e., each child has not had a simulation
                    node.children.indexOfFirst { it.simulations == 0 }
                } else {
                    node.children.map {
                        (it.score / it.simulations) +
                                (cParameter * kotlin.math.sqrt(ln(node.simulations.toDouble()) / it.simulations))
                    }.let { values ->
                        values.indices.maxByOrNull { values[it] }!!
                    }
                }

            simChoices[index].let {
                simState.choicePlayer.makeCardDecision(it, simState)
            }

            // TODO: wrap this up in a method
            var nextChoices = simState.context.getCardChoices(simState.choicePlayer, simState.board)
            while(true) {
                if (nextChoices.size == 1) {
                    simState.choicePlayer.makeCardDecision(nextChoices[0], simState)
                } else when (simState.context) {
                    ChoiceContext.ACTION -> { // must play plus actions first (MPPAF)
                        nextChoices.filterNotNull().firstOrNull { it.addActions > 0 }?.let {
                            simState.choicePlayer.makeCardDecision(it, simState)
                        } ?: break
                    }
                    ChoiceContext.TREASURE -> simState.choicePlayer.makeCardDecision(nextChoices[0], simState)
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
                    choiceContext = simState.context)
            }

            val rolloutResults = rollout(simState)
            node.score = rolloutResults[node.playerNumber!!]!!

            // backpropagation
            var current = node.parent
            while(current != null) {
                current.score += rolloutResults[current.playerNumber]!!
                current.simulations += 1
                current = current.parent
            }
        }
    }


    val iterations = 100000
    val shuffledState = getNewState(state, true)
    for (it in 1..iterations) {
        state.logger?.addPlayout()
        forward(root, getNewState(shuffledState), choices)
    }

    state.logger?.addDecision()

    val simulations: List<Int> = root.children.map { it.simulations }
    val index = simulations.indices.maxByOrNull { simulations[it] }!!
    return choices[index]
}