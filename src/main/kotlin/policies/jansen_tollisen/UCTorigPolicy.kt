package policies.jansen_tollisen

import engine.*
import engine.Player
import mcts.MCTSTreeNode
import policies.rollout.randomPolicy
import kotlin.math.ln

val UCTorigPolicy = fun(
    state: GameState,
    player: Player,
    context: ChoiceContext,
    choice: Choice
): Decision {

    // play action cards first (MPPAF)
    if(context == ChoiceContext.ACTION && choice.isNotEmpty()) {
        val actionCard = (choice as List<Card>).firstOrNull { it.addActions > 0 }
        if (actionCard != null) {
            return Decision(choice, context, choice.indexOf(actionCard))
        }
    }

    // TODO: pull out into settings either at Policy or Simulation level or both
    val playerNumber = PlayerNumber.PlayerTwo
    val seconds = 1
    val cParameter = 0.7
    val root = MCTSTreeNode(player = playerNumber)
    for(possibleDecision in choice.indices) {
        root.addChild(possibleDecision, playerNumber)
    }



    // Note that toMutableList is used below to create copies of the current state.
    // TODO: this could be a bottleneck? think about it.
    fun getNewState(currentState: GameState): GameState {
        val playerOne = Player(
            "Opponent",
            PlayerNumber.PlayerOne,
            randomPolicy,
            currentState.playerOne.deck.toMutableList(),
            currentState.playerOne.hand.toMutableList(),
            currentState.playerOne.inPlay.toMutableList(),
            currentState.playerOne.discard.toMutableList()
        )
        val playerTwo = Player(
            "Self",
            PlayerNumber.PlayerTwo,
            randomPolicy,
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
            simState.next()
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

    fun forward(node: MCTSTreeNode, simState: GameState, simChoice: Choice) {
        if(node.children.size > 0) {
            val simDecision = if(node.children.any { it.simulations == 0 }) {
                node.children.indexOf(node.children.first { it.simulations == 0 })
            } else {
                val menu: List<Double> = node.children.map {
                    (it.score / it.simulations) +
                            (cParameter * kotlin.math.sqrt(ln(node.simulations.toDouble()) / it.simulations))
                }
                menu.indexOf(menu.maxOf { it })
            }
            simState.choicePlayer.makeDecision(simState, Decision(simChoice, simState.context, simDecision))
            var nextChoice = simState.context.getChoice(simState, simState.choicePlayer)
            while(nextChoice.isEmpty()) {
                simState.choicePlayer.makeDecision(simState, Decision(nextChoice, simState.context, null))
                nextChoice = simState.context.getChoice(simState, simState.choicePlayer)
            }

            forward(node.children[simDecision], simState, simState.context.getChoice(simState, simState.choicePlayer))
        } else {

            val rolloutResults = rollout(simState)

            node.simulations = 1
            node.score = rolloutResults[node.player!!]!!
            for(index in simChoice.indices) {
                node.addChild(index, simState.currentPlayer.playerNumber)
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
        forward(root, getNewState(state), choice)
    }
    println(count) // TODO

    state.logger.playouts += count
    state.logger.decisions += 1

    val simulations: List<Int> = root.children.map { it.simulations }
    val maxSim = simulations.maxOf { it }
    if(maxSim == 0) { // TODO: needs a comment
        state.concede = true
    } // TODO: make sure that players can pick to play no action
    return Decision(choice, context, simulations.indexOf(maxSim) )
}