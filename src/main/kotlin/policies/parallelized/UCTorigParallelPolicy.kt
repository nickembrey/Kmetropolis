package policies.parallelized

import engine.*
import engine.card.Card
import engine.player.PlayerNumber
import mcts.MCTSTreeNode
import mcts.RolloutResult
import policies.Policy
import policies.PolicyName
import policies.rollout.jansen_tollisen.EpsilonHeuristicGreedyPolicy
import util.SimulationTimer
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.ln
import kotlin.math.sqrt

class UCTorigParallelPolicy : Policy() {

    val threadPool: ExecutorService = Executors.newWorkStealingPool()

    override val name = PolicyName("UCTorigParallelPolicy")
    override fun policy(
        state: GameState,
        choices: CardChoices
    ): Card? { // TODO: we can get the choices from the state ourselves
        val forwardTimer = SimulationTimer()
        val decisionTimer = SimulationTimer().start()

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
            choiceContext = state.context,
        ).also { it.choices = choices }

        val rolloutResults: Queue<RolloutResult> = ConcurrentLinkedQueue()

        val treeNodeList: MutableList<MCTSTreeNode> = Collections.synchronizedList(ArrayList())

        // TODO: remove subfunctions from here and in MCTS policy
        // Note that toMutableList is used below to create copies of the current state.
        // TODO: this could be a bottleneck? think about it.
        // TODO: epsilonHeuristicGreedyPolicy

        fun rollout(index: Int, simState: GameState) {
            while (!simState.gameOver) {
                simState.makeNextCardDecision(distinctChoices = false)
            }

            val playerOneVp = simState.playerOne.vp
            val playerTwoVp = simState.playerTwo.vp

            val playerOneScore = (if (playerOneVp > playerTwoVp) 1.0 else 0.0) +
                    (playerOneVp - playerTwoVp).toDouble() / 100.0
            val playerTwoScore = (if (playerTwoVp > playerOneVp) 1.0 else 0.0) +
                    (playerTwoVp - playerOneVp).toDouble() / 100.0

            // TODO: 100 is a magic number
            rolloutResults.add(RolloutResult(index, mapOf(
                PlayerNumber.PLAYER_ONE to playerOneScore,
                PlayerNumber.PLAYER_TWO to playerTwoScore)))
        }

        fun forward(
            node: MCTSTreeNode,
            simState: GameState,
            simChoices: CardChoices
        ) {

            if (node.children.isNotEmpty()) {
                val index =
                    if (node.children.size == 1) {
                        0 // TODO: this should never happen, right?
                    } else if (node.children.any { it.simulations.equals(0) }) { // i.e., each child has not had a simulation
                        node.children.indexOfFirst { it.simulations.equals(0) }
                    } else {
                        node.children.map {
                            val simulations = it.simulations.get()
                            val inProcess = it.inProcess.get()
                            (it.score / simulations) +
                                    (cParameter * sqrt(ln(node.simulations.toDouble() + node.inProcess.toDouble()) / (simulations + inProcess)))
                        }.let { values ->
                            values.indices.maxByOrNull { values[it] }!!
                        }
                    }

                simChoices[index].let {
                    simState.makeCardDecision(it)
                }

                var nextChoices = node.children[index].choices
                if(nextChoices == null) { // TODO: wrap this up in a method
                    nextChoices = simState.context.getCardChoices(simState.choicePlayer, simState.board)
                    while (true) {
                        if (nextChoices!!.size == 1) { // TODO
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
                    node.children[index].choices = nextChoices
                }


                return forward(node.children[index], simState, nextChoices!!)
            } else {

                treeNodeList.add(node)
                node.index = treeNodeList.indexOf(node)
                var innerCurrent: MCTSTreeNode? = node
                while(innerCurrent != null) {
                    innerCurrent.inProcess.incrementAndGet()
                    innerCurrent = innerCurrent.parent
                }

                node.children = simChoices.mapIndexed { index, it ->
                    MCTSTreeNode(
                        parent = node,
                        playerNumber = simState.choicePlayer.playerNumber,
                        card = it,
                        choiceContext = simState.context
                    )
                }

                rollout(node.index!!, simState)

            }
        }

        val shuffledState = state.copy(
            newPolicies = Pair(EpsilonHeuristicGreedyPolicy(), EpsilonHeuristicGreedyPolicy()),
            newTrueShuffle = false,
            newLogger = null,
            newMaxTurns = 40, // TODO:
            newPoliciesInOrder = true,
            obfuscateUnseen = true
        )

        val iterations = 10000
        var queued = 0
        var processed = 0

        forwardTimer.start()

        while(processed < iterations) {

            if(queued < iterations) {
                state.logger?.startSimulation()
                threadPool.execute {
                    forward(
                        node = root,
                        simState = shuffledState.copy(),
                        simChoices = choices
                    )
                }
                queued += 1
                state.logger?.endSimulation()
            }

            val result = rolloutResults.poll()
            if(result != null) {
                val node = treeNodeList[result.index]
                node.score += result.scores[node.playerNumber]!!

                var current: MCTSTreeNode? = node
                while (current != null) {
                    current.score += result.scores[current.playerNumber]!!
                    current.inProcess.decrementAndGet()
                    current.simulations.incrementAndGet()
                    current = current.parent
                }
                processed += 1
            }
        }

        forwardTimer.stop()

        val simulations: List<Int> = root.children.map { it.simulations.get() }
        val index = simulations.indices.maxByOrNull { simulations[it] }!!

        decisionTimer.stop()
        return choices[index].also { state.logger?.endDecision() }

    }

}