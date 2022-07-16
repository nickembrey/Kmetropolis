package policies

import engine.*
import engine.card.Card
import engine.player.PlayerNumber
import mcts.DefaultMCTSTreeNode
import mcts.RolloutResult
import policies.jansen_tollisen.EpsilonHeuristicGreedyPolicy
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executor
import kotlin.math.ln
import kotlin.math.sqrt

// TODO: maybe we should hold onto a reference to the game state instead of passing it in every time we need it
abstract class MCTSPolicy( // TODO: need a way to log these in DominionLogger
    private val cParameter: Double,
    private val rollouts: Int
) : Policy() {

    abstract val executor: Executor

    // TODO: clear these lists in between playouts

    private val nodeList: MutableList<DefaultMCTSTreeNode> = Collections.synchronizedList(ArrayList())
    private fun MutableList<DefaultMCTSTreeNode>.addNode(node: DefaultMCTSTreeNode) =
        add(node).also { node.index = indexOf(node) }


    private val rolloutResults: Queue<RolloutResult> = ConcurrentLinkedQueue()
    private fun Queue<RolloutResult>.addResult(
        nodeIndex: Int,
        scores: Map<PlayerNumber, Double>
    ) = add(RolloutResult(nodeIndex, scores))

    private val actionAndTreasurePolicy = policies.utility.MPPAFPolicy() // TODO:

    enum class BackpropProperty {
        PLAYER_ONE_SCORE, PLAYER_TWO_SCORE, SIMULATIONS, IN_PROCESS;
    }

    fun backpropagate(
        node: DefaultMCTSTreeNode,
        property: BackpropProperty,
        value: Number = 1
    ) {
        when(property) {
            BackpropProperty.PLAYER_ONE_SCORE -> {
                if(node.playerNumber == PlayerNumber.PLAYER_ONE) {
                    node.score += value.toDouble()
                }
            }
            BackpropProperty.PLAYER_TWO_SCORE -> {
                if(node.playerNumber == PlayerNumber.PLAYER_TWO) {
                    node.score += value.toDouble()
                }
            }
            BackpropProperty.SIMULATIONS -> node.simulations.getAndAdd(value.toInt())
            BackpropProperty.IN_PROCESS -> node.inProcess.getAndAdd(value.toInt())
        }
        if(node.parent != null) {
            backpropagate(node.parent, property, value)
        }
    }

    // TODO: use MCTSTreeNode, not some specific implementation if possible
    private fun getNodeValue(node: DefaultMCTSTreeNode): Double {
        val parent = node.parent!!
        // TODO: get rid of the "get"
        return (node.score / node.simulations.get()) +
                (cParameter * sqrt(ln
                    (parent.simulations.toDouble() + node.inProcess.toDouble()) /
                        (node.simulations.get() + node.inProcess.get())
                ))
    }

    private fun getNextNode(node: DefaultMCTSTreeNode): DefaultMCTSTreeNode {
        assert(node.children.isNotEmpty()) // TODO:
        val nextNode = node.children
            .indexOfFirst { it.simulations.equals(0) }
            .takeUnless { it == -1 } ?: node.children
            .map { getNodeValue(it) }
            .let { values -> values.indices.maxByOrNull { values[it] }!! }
            .let { node.children[it] }
        return nextNode as DefaultMCTSTreeNode // TODO: need for cast seems to be a compiler bug?
    }

    private fun getNextChoices(simState: GameState, defaultDecisions: List<Card?> = listOf()): List<List<Card?>> {
        val nextChoices = simState.context.getCardChoices(simState.choicePlayer, simState.board)
        return if (nextChoices.size == 1) { // TODO
            simState.makeCardDecision(nextChoices[0])
            getNextChoices(simState, defaultDecisions + nextChoices[0])
        } else when (simState.context) {
            ChoiceContext.ACTION, ChoiceContext.TREASURE -> {
                val card = actionAndTreasurePolicy.policy(simState, nextChoices)
                simState.makeCardDecision(card)
                getNextChoices(simState, defaultDecisions + card)
            }
            else -> nextChoices.map { defaultDecisions + it }
        }
    }

    private fun rollout(rolloutState: GameState): Map<PlayerNumber, Double> {
        while (!rolloutState.gameOver) {
            rolloutState.makeNextCardDecision()
        }

        return rolloutState.players.associate {
            val opponentVp = it.playerNumber.getOpponent(rolloutState).vp
            it.playerNumber to (it.vp - opponentVp) / 100.0 + if (it.vp > opponentVp) 1.0 else 0.0
        }
    }

    private fun forward(
        simState: GameState,
        node: DefaultMCTSTreeNode
    ) {

        if (node.children.isNotEmpty()) {

            val nextNode = getNextNode(node)
            for(card in nextNode.cards) {
                simState.makeCardDecision(card)
            }

            // TODO: by only using nodes to make decisions, we're not letting the simState
            //       get caught up; it's skipping 1-choice decisions
            //       solution? maybe allow each node to have multiple choices to run through in order
            return forward(simState, nextNode)
        } else {

            backpropagate(node, BackpropProperty.IN_PROCESS)
            nodeList.addNode(node)

            // TODO: use addChildren
            node.children = getNextChoices(simState).map {
                DefaultMCTSTreeNode(
                    parent = node,
                    playerNumber = simState.choicePlayer.playerNumber,
                    cards = it,
                    choiceContext = simState.context
                )
            }

            val rolloutResult = rollout(simState)
            rolloutResults.addResult(node.index!!, rolloutResult)

        }
    }

    private fun simulationPolicy(state: GameState, choices: CardChoices): Card? {

        val root = DefaultMCTSTreeNode.getRoot(state)

        val shuffledState = state.copy(
            newPolicies = Pair(EpsilonHeuristicGreedyPolicy(), EpsilonHeuristicGreedyPolicy()),
            newLogger = null,
            newMaxTurns = 999, // TODO: think about this
            newPoliciesInOrder = true,
            deterministicDecks = true, // TODO:
            obfuscateUnseen = true
            // TODO: will this still be necessary after using the FrequencyDeck?
            //       well, the opponent's hand will still need to be shuffled, I guess
            //       maybe we need a FrequencyHand too
        )

        var queued = 0
        var processed = 0 // TODO: replace with simulations at root?
        var result: RolloutResult?

        while(processed < rollouts) {

            result = rolloutResults.poll() // TODO: replace with do / while
            while(result != null) {
                val node = nodeList[result.index]

                backpropagate(node, BackpropProperty.PLAYER_ONE_SCORE, result.scores[PlayerNumber.PLAYER_ONE]!!) // TODO:
                backpropagate(node, BackpropProperty.PLAYER_TWO_SCORE, result.scores[PlayerNumber.PLAYER_TWO]!!)
                backpropagate(node, BackpropProperty.IN_PROCESS, -1)
                backpropagate(node, BackpropProperty.SIMULATIONS)

                processed += 1
                result = rolloutResults.poll()
            }

            if(queued < rollouts) {
                state.logger?.startSimulation() // TODO: this might not make sense if there's a timer, etc.
                executor.execute {
                    forward(
                        node = root,
                        simState = shuffledState.copy(),
                    )
                }
                queued += 1
                state.logger?.endSimulation()
            }
        }

        val simulations: List<Int> = root.children.map { it.simulations.get() }
        val index = simulations.indices.maxByOrNull { simulations[it] }!!

        assert(rolloutResults.isEmpty())
        nodeList.clear()

        return choices[index]
    }

    override fun policy(
        state: GameState,
        choices: CardChoices
    ): Card? { // TODO: we can get the choices from the state ourselves

        state.logger?.startDecision()

        return when(state.context) {
            // TODO: the MPPAF is causing us to make bad decisions on action cards that don't give actions
            ChoiceContext.ACTION, ChoiceContext.TREASURE -> actionAndTreasurePolicy.policy(state, choices)
            else -> simulationPolicy(state, choices)
        }.also { state.logger?.endDecision() }
    }

}