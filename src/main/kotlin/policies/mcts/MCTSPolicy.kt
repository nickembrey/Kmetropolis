package policies.mcts

import engine.*
import engine.branch.BranchSelection
import engine.branch.BranchContext
import engine.card.CardType
import engine.operation.stack.StackOperation
import engine.player.PlayerNumber
import logger
import policies.delegates.draw.RandomDrawPolicy
import ml.NaiveBayesResourceCompositionWeightSource
import policies.Policy
import policies.delegates.action.MPPAFPolicy
import policies.playout.score.PlayoutScoreFn
import java.lang.Integer.max
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executor
import kotlin.math.ln
import kotlin.math.sqrt

// TODO: maybe we should hold onto a reference to the game state instead of passing it in every time we need it
abstract class MCTSPolicy( // TODO: need a way to log these in DominionLogger
    protected val cParameter: Double,
    protected val rollouts: Int,
    protected val rolloutPolicy: Policy,
    protected val rolloutScoreFn: PlayoutScoreFn,
    protected val useNBCWeights: Boolean
) : Policy() { // TODO: make rolloutPolicy a parameter

    abstract val stateCopies: Int
    abstract val executor: Executor

    protected var maxDepth: Int = 0
    protected var maxTurns: Int = 0
    protected val contextMap: MutableMap<BranchContext, Int> = mutableMapOf()

    protected val nbcClassifier = NaiveBayesResourceCompositionWeightSource()

    // TODO: clear these lists in between playouts

    protected val nodeList: MutableList<MCTSTreeNode> = Collections.synchronizedList(ArrayList())
    protected fun MutableList<MCTSTreeNode>.addNode(node: MCTSChildNode) =
        add(node).also { node.index = indexOf(node) } // TODO: I think a HashMap would be better

    protected val stateQueue: Queue<GameState> = ConcurrentLinkedQueue()

    protected val rolloutResults: Queue<ExecutionResult> = ConcurrentLinkedQueue()
    protected fun Queue<ExecutionResult>.addResult(
        nodeIndex: Int,
        scores: Map<PlayerNumber, Double>
    ) = add(RolloutResult(nodeIndex, scores))


    // TODO: need a delegate map so we can factor out getNextOptions
    private val drawPolicy = RandomDrawPolicy()
    private val actionPolicy = MPPAFPolicy()
    private val treasurePolicy = policies.utility.PlayAllTreasuresPolicy()

    enum class BackpropProperty {
        PLAYER_ONE_SCORE, PLAYER_TWO_SCORE, SIMULATIONS, IN_PROCESS;
    }

    private fun backpropagate(
        node: MCTSTreeNode,
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
            BackpropProperty.SIMULATIONS -> node.completedRollouts.getAndAdd(value.toInt())
            BackpropProperty.IN_PROCESS -> node.currentRollouts.getAndAdd(value.toInt())
        }
        if(node is MCTSChildNode) {
            backpropagate(node.parent, property, value)
        }
    }

    // TODO: it would be nice to pass a regime into a function on the node and get back the value
    protected fun getNodeValue(node: MCTSChildNode): Double {
        val parent = node.parent
        // TODO: get rid of the "get"
        return when(node) { // score term
            is DrawChildNode -> {
                when(node.playerNumber) { // best draws for opponent, but not for self
                    node.rootPlayerNumber -> 0.0
                    else -> (node.score / node.completedRollouts.get())
                }
            }
            else -> (node.score / node.completedRollouts.get())
        }.let { it + // variance term
                (cParameter * sqrt(ln(parent.completedRollouts.toDouble() + node.currentRollouts.toDouble()) /
                (node.completedRollouts.get() + node.currentRollouts.get()))) }
            .let { it * if(it > 0) node.weight else (1 / node.weight) } // weight term
    }

    // calculate which node to move forward on
    private fun getNextNode(node: MCTSTreeNode): MCTSChildNode = node.children
        .firstOrNull { it.completedRollouts.equals(0) }
        ?: node.children
            .map { getNodeValue(it) }
            .let { values -> values.indices.maxByOrNull { values[it] }!! }
            .let { node.children[it] }

    // TODO: this should be put in MCTSChildNode
    protected fun getNextOptions(simState: GameState): Collection<BranchSelection> {
        var nextContext = simState.getNextBranchContext()
        var nextOptions = nextContext.toOptions(simState)
        while(true) {
            if (nextOptions.size == 1) { // TODO
                simState.processBranchSelection(nextContext, nextOptions.first())
                nextContext = simState.getNextBranchContext()
                nextOptions = nextContext.toOptions(simState)
            } else when (nextContext) {
                BranchContext.CHOOSE_ACTION -> {
                    if(simState.currentPlayer.hand.firstOrNull { it.addActions > 0 } != null || simState.currentPlayer.hand.filter { it.type == CardType.ACTION }.size < 2) {
                        val selection = actionPolicy.policy(simState)
                        simState.processBranchSelection(nextContext, selection)
                        nextContext = simState.getNextBranchContext()
                        nextOptions = nextContext.toOptions(simState)
                    } else {
                        return nextOptions
                    }
                }
                BranchContext.CHOOSE_TREASURE -> {
                    val selection = treasurePolicy.policy(simState)
                    simState.processBranchSelection(nextContext, selection)
                    nextContext = simState.getNextBranchContext()
                    nextOptions = nextContext.toOptions(simState)
                }
                else -> return nextOptions
//                .also { simState.eventStack.push(nextContext)} // so the rollout doesn't skip the decision
            }
        }

    }

    protected open fun prepareRollout(node: MCTSChildNode, simState: GameState) {

        if(node.depth > maxDepth) maxDepth = node.depth
        if(node.turns > maxTurns) maxTurns = node.turns
        contextMap.merge(node.context, 1, Int::plus)

        for(op in node.history) {
            simState.processOperation(op)
        }
        simState.eventStack = node.eventStack.copy()
//        node.history.clear() // TODO: why was this breaking things?
        // TODO: clear eventStack, or transform node into non-leaf type

        simState.processBranchSelection(node.context, node.selection)

        val selections = getNextOptions(simState)

        if(useNBCWeights && simState.context == BranchContext.CHOOSE_BUY) {
            node.children.addAll(
                MCTSChildNode.getChildren(
                    state = simState,
                    parent = node,
                    history = simState.operationHistory.toMutableList(),
                    eventStack = simState.eventStack.copy(),
                    selections = selections,
                    weightSource = nbcClassifier)
            )
        } else {
            node.children.addAll(
                MCTSChildNode.getChildren(
                    state = simState,
                    parent = node,
                    history = simState.operationHistory.toMutableList(),
                    eventStack = simState.eventStack.copy(),
                    selections = selections)
            )
        }


        if(node is DecisionChildNode) { // only playout decisions
            backpropagate(node, BackpropProperty.IN_PROCESS)
            nodeList.addNode(node)
            val rolloutResult = rollout(simState)
            rolloutResults.addResult(node.index, rolloutResult)
        } else {
            rolloutResults.add(NO_ROLLOUT)
        }
    }

    protected open fun rollout(rolloutState: GameState): Map<PlayerNumber, Double> {
        logger.initPlayout() // TODO: this might not make sense if there's a timer, etc.
        while (!rolloutState.gameOver) {
            rolloutState.processNextBranch()
        }
        logger.recordPlayout()


        // TODO: see note in PlayerCards
//        rolloutState.undoOperationHistory()
//        stateQueue.add(rolloutState)

        return rolloutScoreFn(rolloutState)
    }

    protected open fun forward(
        simState: GameState,
        node: MCTSTreeNode
    ) {
        // TODO: non-recursive?
        if (node.children.isNotEmpty()) {
            return forward(simState, getNextNode(node))
        } else if(node is MCTSChildNode) {
            prepareRollout(node, simState)
        } else { // TODO: a little hacky
            throw IllegalStateException()
        }
    }

    protected open fun simulationPolicy(state: GameState): BranchSelection {

        nodeList.clear()
        rolloutResults.clear()
        stateQueue.clear()

        maxDepth = 0
        maxTurns = 0

        val root = if(useNBCWeights && state.context == BranchContext.CHOOSE_BUY) {
            RootNode.new(state, nbcClassifier)
        } else {
            RootNode.new(state)
        }

        val shuffledState = state.copy(
            newPolicies = listOf(rolloutPolicy, rolloutPolicy),
            newMaxTurns = 999, // TODO: think about this
            newLog = false)
        stateQueue.add(shuffledState)

        // simulate a redraw of the opponent's hand
        shuffledState.eventStack.push(StackOperation.OPPONENT_HAND_REDRAW(
            state.currentPlayerNumber,
            state.otherPlayer.hand.size)
        )

        for(i in 2..stateCopies) {
            stateQueue.add(shuffledState.copy())
        }

        var queued = 0
        var draws = 0
        var processed = 0 // TODO: replace with simulations at root?

        while(processed < rollouts) {
            while(rolloutResults.isNotEmpty()) {
                when(val next = rolloutResults.remove()) {
                    is NO_ROLLOUT -> draws += 1
                    is RolloutResult -> {
                        val node = nodeList[next.index]

                        backpropagate(node, BackpropProperty.PLAYER_ONE_SCORE, next.scores[PlayerNumber.PLAYER_ONE]!!) // TODO:
                        backpropagate(node, BackpropProperty.PLAYER_TWO_SCORE, next.scores[PlayerNumber.PLAYER_TWO]!!)
                        backpropagate(node, BackpropProperty.IN_PROCESS, -1)
                        backpropagate(node, BackpropProperty.SIMULATIONS)

                        processed += 1
                    }
                    else -> throw IllegalStateException()
                }
            }

            if(queued - draws < rollouts) {
                executor.execute {
                    forward(
                        node = root,
                        simState = shuffledState.copy(
                            newMaxTurns = max(50, maxTurns + 10)
                        )
                    ) }
                queued += 1
            }
        }

        val simulations: List<Int> = root.children.map { it.completedRollouts.get() }
        val index = simulations.indices.maxByOrNull { simulations[it] }!!

        assert(rolloutResults.isEmpty()) // TODO: make sure will fail
        nodeList.clear()

        return root.children[index].selection // TODO: hacky
    }

    override fun policy(
        state: GameState
    ): BranchSelection { // TODO: we can get the choices from the state ourselves

        logger.initDecision()

        return when(state.context) {
            // TODO: the MPPAF is causing us to make bad decisions on action cards that don't give actions
            BranchContext.DRAW -> drawPolicy.policy(state)
            BranchContext.CHOOSE_ACTION -> {
                if(state.currentPlayer.hand.firstOrNull { it.addActions > 0 } != null || state.currentPlayer.hand.filter { it.type == CardType.ACTION }.size < 2) {
                    actionPolicy.policy(state)
                } else {
                    simulationPolicy(state)
                }
            }
            BranchContext.CHOOSE_TREASURE -> treasurePolicy.policy(state)
            else -> simulationPolicy(state)
        }.also { logger.recordDecision(contextMap, maxDepth, maxTurns - state.turns) }
    }

}