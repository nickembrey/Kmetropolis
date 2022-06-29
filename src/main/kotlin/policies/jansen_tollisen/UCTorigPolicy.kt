package policies.jansen_tollisen

import engine.*
import engine.Player
import engine.simulation.getNextCardChoices
import engine.simulation.makeNextCardDecision
import mcts.MCTSTreeNode
import policies.rollout.randomPolicy
import kotlin.math.ln

fun UCTorigPolicy(
    state: GameState,
    player: Player,
    context: ChoiceContext,
    choices: CardChoices
): DecisionIndex {

    // play action cards first (MPPAF)
    if(context == ChoiceContext.ACTION) {
        val actionCard = choices.filterNotNull().firstOrNull { it.addActions > 0 }   // TODO:
        if (actionCard != null) { // TODO: will it ever be null?
            return choices.indexOf(actionCard)
        }
    }

    if(context == ChoiceContext.TREASURE) {
        return 0 // TODO: this is wrong -- see MCTSPolicy
    }

    // TODO: pull out into settings either at Policy or Simulation level or both
    val playerNumber = PlayerNumber.PlayerTwo
    val seconds = 1
    val cParameter = 0.7
    val root = MCTSTreeNode(player = playerNumber)
    for(possibleDecision in choices.indices) {
        root.addChild(possibleDecision, playerNumber, choice = choices[possibleDecision])
    }


    // TODO: remove subfunctions from here and in MCTS policy
    // Note that toMutableList is used below to create copies of the current state.
    // TODO: this could be a bottleneck? think about it.
    fun getNewState(currentState: GameState): GameState {
        val playerOne = Player(
            "Opponent",
            PlayerNumber.PlayerOne,
            ::randomPolicy,
            currentState.playerOne.deck.toMutableList(),
            currentState.playerOne.hand.toMutableList(),
            currentState.playerOne.inPlay.toMutableList(),
            currentState.playerOne.discard.toMutableList()
        )
        val playerTwo = Player(
            "Self",
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
            noShuffle=true).apply { currentPlayer = playerTwo }
    }

    fun rollout(simState: GameState): Map<PlayerNumber, Double> {

        // TODO: it shouldn't be the case that the policy is always playerTwo

        while(!simState.gameOver) {
            simState.makeNextCardDecision(::randomPolicy) // TODO: is this the right policy?
        }

        val playerOneVp = simState.playerOne.vp
        val playerTwoVp = simState.playerTwo.vp


        val playerOneScore = (if(playerOneVp > playerTwoVp) 1.0 else 0.0) +
                (simState.playerOne.vp - simState.playerTwo.vp).toDouble() / 100.0
        val playerTwoScore = (if(playerTwoVp > playerOneVp) 1.0 else 0.0) +
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

            forward(node.children[simDecisionIndex], simState, simState.getNextCardChoices())
        } else {

            val rolloutResults = rollout(simState)

            node.simulations = 1
            node.score = rolloutResults[node.player!!]!!
            for(index in simChoices.indices) {
                node.addChild(index, simState.currentPlayer.playerNumber, choice = simChoices[index])
            }

            // backpropagation
            var current = node.parent
            while(current != null) {
                current.score += rolloutResults[current.player]!!
                current.simulations += 1
                current = current.parent
            }
        }
    }

    var count = 0
    val end = System.currentTimeMillis() + seconds * 1000
    while (System.currentTimeMillis() < end) {
        count += 1
        forward(root, getNewState(state), choices)
    }
    println(count) // TODO

    if(state.logger != null) {
        state.logger.addPlayout()
        state.logger.addDecision()
    }

    val simulations: List<Int> = root.children.map { it.simulations }
    val maxSim = simulations.maxOf { it }
    if(maxSim == 0) { // TODO: needs a comment
        state.concede = true
    } // TODO: make sure that players can pick to play no action
    return simulations.indexOf(maxSim)
}