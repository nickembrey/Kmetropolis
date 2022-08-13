package policies.mcts

import engine.*
import engine.branch.Branch
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
import policies.heuristic.DevelopmentPolicy
import policies.jansen_tollisen.EpsilonHeuristicGreedyPolicy
import policies.playout.GreenRolloutPolicy
import policies.playout.score.PlayoutScoreFn
import policies.utility.RandomPolicy
import java.lang.Integer.max
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executor
import kotlin.math.ln
import kotlin.math.sqrt

// TODO: pick either "rollouts" or "playouts"
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

     private var rolloutPolicyMenu = arrayOf(0.0, 0.0, 0.0, 0.0)

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
            is DrawChildNode -> node.weight
            else -> (node.score / node.completedRollouts.get()).let { it + // variance term
                    (cParameter * sqrt(ln(parent.completedRollouts.toDouble() + node.currentRollouts.toDouble()) /
                            (node.completedRollouts.get() + node.currentRollouts.get()))) }
                .let { it * node.weight } // weight term
        }
    }

    // calculate which node to move forward on
    private fun getNextNode(node: MCTSTreeNode): MCTSChildNode = node.children
        .firstOrNull { it.completedRollouts.equals(0) }
        ?: node.children
            .map { getNodeValue(it) }
            .let { values -> values.indices.maxByOrNull { values[it] }!! }
            .let { node.children[it] }

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
        if(node is MCTSChildNode) {
            if(node.depth > maxDepth) maxDepth = node.depth
            if(node.turns > maxTurns) maxTurns = node.turns
            contextMap.merge(node.context, 1, Int::plus)
        }

        when(node) {
            is RootNode -> forward(simState, getNextNode(node))
            is DecisionChildNode -> when(node.completedRollouts.get()) {
                0 -> {
                    for(op in node.history) {
                        simState.processOperation(op)
                    }
                    simState.eventStack = node.eventStack.copy()
                    backpropagate(node, BackpropProperty.IN_PROCESS)
                    nodeList.addNode(node)
                    val rolloutResult = rollout(simState)
                    rolloutResults.addResult(node.index, rolloutResult)

                    if(simState.players[0].policy.name == GreenRolloutPolicy().name) {
                        rolloutPolicyMenu[0] += rolloutResult[PlayerNumber.PLAYER_ONE]!!
                        rolloutPolicyMenu[1] += 1.0
                    } else if(simState.players[0].policy.name == DevelopmentPolicy().name) {
                        rolloutPolicyMenu[2] += rolloutResult[PlayerNumber.PLAYER_ONE]!!
                        rolloutPolicyMenu[3] += 1.0
                    }

                    if(simState.players[1].policy.name == GreenRolloutPolicy().name) {
                        rolloutPolicyMenu[0] += rolloutResult[PlayerNumber.PLAYER_TWO]!!
                        rolloutPolicyMenu[1] += 1.0
                    } else if(simState.players[1].policy.name == DevelopmentPolicy().name) {
                        rolloutPolicyMenu[2] += rolloutResult[PlayerNumber.PLAYER_TWO]!!
                        rolloutPolicyMenu[3] += 1.0
                    }

                }
                1 -> {
                    val copy = simState.copy() // TODO: clean up?
                    for(op in node.history) {
                        copy.processOperation(op)
                    }
                    copy.eventStack = node.eventStack.copy()

                    node.children.addAll(MCTSChildNode.getChildren(
                        state = copy,
                        parent = node,
                        actionPolicy = actionPolicy,
                        treasurePolicy = treasurePolicy))

                    forward(simState, getNextNode(node))
                }
                else -> forward(simState, getNextNode(node))
            }
            is DrawChildNode -> when(node.completedRollouts.get()) {
                0 -> {
                    val copy = simState.copy() // TODO: clean up?
                    for(op in node.history) {
                        copy.processOperation(op)
                    }
                    copy.eventStack = node.eventStack.copy()

                    node.children.addAll(MCTSChildNode.getChildren(
                        state = copy,
                        parent = node,
                        actionPolicy = actionPolicy,
                        treasurePolicy = treasurePolicy))

                    forward(simState, getNextNode(node))
                }
                else -> forward(simState, getNextNode(node))
            }
            else -> throw IllegalStateException()
        }
    }

    protected open fun simulationPolicy(state: GameState, branch: Branch): BranchSelection {


        rolloutPolicyMenu = arrayOf(0.0, 0.0, 0.0, 0.0)
        nodeList.clear()
        rolloutResults.clear()
        stateQueue.clear()

        maxDepth = 0
        maxTurns = 0

        val shuffledState = state.copy(
            newPolicies = listOf(rolloutPolicy, rolloutPolicy),
            newMaxTurns = 999, // TODO: think about this
            newLog = false)

        stateQueue.add(shuffledState)

        // simulate a redraw of the opponent's hand
        shuffledState.eventStack.push(StackOperation.OPPONENT_HAND_REDRAW(
            state.currentPlayerNumber,
            state.otherPlayer.hand.toMutableList())
        )

        val root = if(useNBCWeights && state.context == BranchContext.CHOOSE_BUYS) {
            RootNode.new(
                state = shuffledState,
                branch = branch,
                actionPolicy = actionPolicy,
                treasurePolicy = treasurePolicy,
                weightSource = nbcClassifier)
        } else {
            RootNode.new(
                state = shuffledState,
                branch = branch,
                actionPolicy = actionPolicy,
                treasurePolicy = treasurePolicy,
            )
        }

        for(i in 2..stateCopies) {
            stateQueue.add(shuffledState.copy())
        }

        var queued = 0
        var processed = 0 // TODO: replace with simulations at root?

        var greenScore = 0.0
        var greedyScore = 0.0

        while(processed < rollouts) {
            while(rolloutResults.isNotEmpty()) {
                when(val next = rolloutResults.remove()) {
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

            val rParameter = 0.8
            val totalPlays = rolloutPolicyMenu[1] + rolloutPolicyMenu[3]
            greenScore = (rolloutPolicyMenu[0] / rolloutPolicyMenu[1]) + rParameter * sqrt(ln(totalPlays) / rolloutPolicyMenu[1])
            greedyScore = (rolloutPolicyMenu[2] / rolloutPolicyMenu[3]) + rParameter * sqrt(ln(totalPlays) / rolloutPolicyMenu[3])

            if(greenScore.isNaN()) greenScore = 0.0
            if(greedyScore.isNaN()) greedyScore = 0.0

//            val policies = listOf(rolloutPolicy, rolloutPolicy)

            val policies = if(greenScore == 0.0) {
                val otherPolicy = listOf(RandomPolicy(), DevelopmentPolicy()).random()
                listOf(rolloutPolicy, otherPolicy).shuffled()
            } else if(greedyScore == 0.0) {
                val otherPolicy = listOf(RandomPolicy(), rolloutPolicy).random()
                listOf(DevelopmentPolicy(), otherPolicy).shuffled()
            } else if(greenScore >= greedyScore) {
                val otherPolicy = listOf(RandomPolicy(), DevelopmentPolicy()).random()
                listOf(rolloutPolicy, otherPolicy).shuffled()
            } else {
                val otherPolicy = listOf(RandomPolicy(), rolloutPolicy).random()
                listOf(DevelopmentPolicy(), otherPolicy).shuffled()
            }


            if(queued < rollouts) {
                executor.execute {
                    forward(
                        node = root,
                        simState = shuffledState.copy(
                            newPolicies = policies,
                            newMaxTurns = max(50, maxTurns + 10)
                        )
                    ) }
                queued += 1
            }
        }

        if(state.turns == 5 || state.turns == 10 || state.turns == 20 || state.turns == 30) {
            greenScore = (rolloutPolicyMenu[0] / rolloutPolicyMenu[1])
            greedyScore = (rolloutPolicyMenu[2] / rolloutPolicyMenu[3])
            print(state.turns)
            print("\nGreen score: $greenScore")
            print("\nDevelopment score: $greedyScore\n")
        }

        val simulations: List<Int> = root.children.map { it.completedRollouts.get() }
        val index = simulations.indices.maxByOrNull { simulations[it] }!!

        assert(rolloutResults.isEmpty()) // TODO: make sure will fail
        nodeList.clear()

        return root.children[index].selections.single() // TODO: hacky
    }

    override fun policy(
        state: GameState,
        branch: Branch
    ): BranchSelection {

        logger.initDecision()

        return when(state.context) {
            // TODO: the MPPAF is causing us to make bad decisions on action cards that don't give actions
            BranchContext.DRAW -> drawPolicy.policy(state, branch)
            BranchContext.CHOOSE_ACTION -> {
                if(state.currentPlayer.hand.firstOrNull { it.addActions > 0 } != null || state.currentPlayer.hand.filter { it.type == CardType.ACTION }.size < 2) {
                    actionPolicy.policy(state, branch)
                } else {
                    simulationPolicy(state, branch)
                }
            }
            BranchContext.CHOOSE_TREASURE -> treasurePolicy.policy(state, branch)
            else -> simulationPolicy(state, branch)
        }.also { logger.recordDecision(contextMap, maxDepth, maxTurns - state.turns) }
    }

}