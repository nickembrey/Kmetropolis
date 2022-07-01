package policies.jansen_tollisen

import engine.*
import engine.Player
import mcts.MCTSTreeNode
import policies.policy.randomPolicy
import kotlin.math.ln

fun _UCTorigPolicy(
    state: GameState,
    choices: CardChoices
): Card? {

    // play action cards first (MPPAF)
    if(state.context == ChoiceContext.ACTION) {
        val addActionsCard = choices.filterNotNull().firstOrNull { it.addActions > 0 }   // TODO:
        if (addActionsCard != null) { // TODO: will it ever be null?
            return addActionsCard
        }
    } else if(state.context == ChoiceContext.TREASURE) {
        return choices[0]
    }

    // TODO: pull out into settings either at Policy or Simulation level or both
    val playerNumber = PlayerNumber.PlayerTwo
    val seconds = 1
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
    fun getNewState(currentState: GameState): GameState {
        val playerOne = Player(
            "Player One",
            PlayerNumber.PlayerOne,
            ::randomPolicy,
            currentState.playerOne.deck.toMutableList(),
            currentState.playerOne.hand.toMutableList(),
            currentState.playerOne.inPlay.toMutableList(),
            currentState.playerOne.discard.toMutableList()
        )
        val playerTwo = Player(
            "Player Two",
            PlayerNumber.PlayerTwo,
            ::randomPolicy,
            currentState.playerTwo.deck.toMutableList(),
            currentState.playerTwo.hand.toMutableList(),
            currentState.playerTwo.inPlay.toMutableList(),
            currentState.playerTwo.discard.toMutableList()
        )
        return GameState(
            playerOne,
            playerTwo,
            currentState.board.toMutableMap(),
            currentState.turns,
            currentState.context,
            trueShuffle = false
        ).apply { currentPlayer = currentState.currentPlayer }
    }

    fun rollout(simState: GameState): Map<PlayerNumber, Double> {

        while(!simState.gameOver) {
            simState.choicePlayer.makeNextCardDecision(simState, ::randomPolicy) // TODO: is this the right policy?
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

        if(node.children.size > 0) {
            val simDecisionIndex = if(node.children.any { it.simulations == 0 }) {
                node.children.indexOf(node.children.first { it.simulations == 0 })
            } else {
                val menu: List<Double> = node.children.map {
                    (it.score / it.simulations) +
                            (cParameter * kotlin.math.sqrt(ln(node.simulations.toDouble()) / it.simulations))
                }
                menu.indexOf(menu.maxOf { it })
            }

            val card = simChoices[simDecisionIndex]!!

            simState.choicePlayer.makeCardDecision(card, simState)

            forward(node.children[simDecisionIndex], simState, simState.context.getCardChoices(simState.choicePlayer, simState.board))
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
                current.score += rolloutResults[current.player]!!
                current.simulations += 1
                current = current.parent
            }
        }
    }


    val end = System.currentTimeMillis() + seconds * 1000
    while (System.currentTimeMillis() < end) {
        state.logger?.addPlayout()
        forward(root, getNewState(state), choices)
    }

    state.logger?.addDecision()

    val simulations: List<Int> = root.children.map { it.simulations }
    val maxSim = simulations.maxOf { it }

    return choices[maxSim]
}