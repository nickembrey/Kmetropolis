package policies

import engine.*
import engine.Player
import mcts.MCTSTreeNode
import policies.rollout.randomPolicy
import kotlin.math.log2

fun MCTSPolicy(
    state: GameState,
    player: Player,
    context: ChoiceContext,
    choices: CardChoices
): Int {

    if (context == ChoiceContext.TREASURE) {
        val firstNotNull = choices.filterNotNull().first()
        return choices.indexOf(firstNotNull)
    }

    val seconds = 1
    val cParameter = 1.4
    val root = MCTSTreeNode()
    for (possibleDecision in choices.indices) {
        root.addChild(
            possibleDecision,
            state.choicePlayer.playerNumber,
            choices[possibleDecision],
            state.context
        )
    }

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
            noShuffle = true
        ).apply { currentPlayer = playerTwo }
    }

    fun rollout(simState: GameState): Int {

        while (!simState.gameOver) {
            while (!simState.gameOver) {
                if (simState.context == ChoiceContext.TREASURE) {
                    val simChoices = context.getCardChoices(simState.choicePlayer, simState.board)
                    val notNull = simChoices.filterNotNull()
                    val decisionIndex = if (notNull.isNotEmpty()) {
                        simChoices.indexOf(notNull.first())
                    } else {
                        0
                    }

                    simState.applyDecision(simChoices, decisionIndex)
                } else {
                    simState.makeNextDecision(::randomPolicy)
                }
            }
        }

        // TODO: big bug! currently weighing moves that opponents make that are bad more highly!!
        return if (simState.playerOne.vp >= simState.playerTwo.vp) {
            0
        } else {
            1
        }
    }

    fun forward(node: MCTSTreeNode, simState: GameState, simChoices: CardChoices) {
        if (node.children.size > 0) { // TODO: this is all awk
            val simDecisionIndex = if (node.children.any { it.simulations == 0 }) {
                node.children.indexOf(node.children.first { it.simulations == 0 })
            } else {
                val menu: List<Double> = node.children.map {
                    (it.wins.toDouble() / it.simulations) +
                            (cParameter * kotlin.math.sqrt(log2(node.simulations.toDouble()) / it.simulations))
                }
                menu.indexOf(menu.maxOf { it })
            }
            simState.applyDecision(simChoices, simDecisionIndex)

            forward(node.children[simDecisionIndex], simState, simState.getNextChoices())
        } else {
            for (index in simChoices.indices) {
                node.addChild(index, simState.choicePlayer.playerNumber, simChoices[index], simState.context)
            }
            node.simulations = 1
            node.wins = rollout(simState)

            // backpropagation
            var current = node.parent
            while (current != null) {
                current.simulations += 1
                current.wins += node.wins
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
    println("Number of simulations: $count")
    println("Wins at root: ${root.wins}")
    if (root.wins == 0) {
        state.concede = true
    }

    state.logger.playouts += count
    state.logger.decisions += 1

    val simulations: List<Int> = root.children.map { it.simulations }
    val maxSim = simulations.maxOf { it }
    return simulations.indexOf(maxSim)
}