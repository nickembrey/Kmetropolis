package policies.mcts

import engine.*
import engine.branch.*
import engine.card.CardType
import engine.player.PlayerNumber
import logger
import policies.CurrentThreadExecutor
import policies.delegates.draw.RandomDrawPolicy
import policies.Policy
import policies.PolicyName
import policies.delegates.action.MPPAFPolicy
import policies.mcts.node.*
import policies.mcts.rollout.score.RolloutScoreFn
import policies.delegates.treasure.PlayAllTreasuresPolicy
import java.lang.Integer.max
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executor

// TODO: shouldn't even have access to the opponent's data, just a view

class MCTSPolicy(
    private val cParameter: Double,
    private val rollouts: Int,
    private val rolloutPolicy: Policy,
    private val rolloutScoreFn: RolloutScoreFn,
    private val nodeValueFn: NodeValueFn,
    private val executor: Executor = CurrentThreadExecutor(),
) : Policy() {

    // TODO: add additional parameters
    override val name = PolicyName(
        "MCTSPolicy ($cParameter, $rollouts, ${rolloutPolicy.name}, ${rolloutScoreFn.name})"
    )

    enum class BackpropProperty {
        PLAYER_ONE_SCORE, PLAYER_TWO_SCORE, COMPLETED, IN_PROCESS;
    }

    // TODO: wrap these statistics up, or maybe use the logger
    // TODO: shouldn't these be concurrency safe?
    private var maxDepth: Int = 0
    private var maxTurns: Int = 0
    private val contextMap: MutableMap<BranchContext, Int> = mutableMapOf()
    private val graphPairs: MutableList<Pair<Int, Int>> = mutableListOf()

    private val drawPolicy = RandomDrawPolicy()
    private val actionPolicy = MPPAFPolicy() // TODO:
    private val treasurePolicy = PlayAllTreasuresPolicy()

    private val rolloutResults: Queue<RolloutResult> = ConcurrentLinkedQueue()

    var root: RootNode? = null

    private fun backpropagate(
        node: MCTSTreeNode,
        property: BackpropProperty,
        value: Number = 1
    ) {
        when(property) {
            BackpropProperty.IN_PROCESS -> node.currentRollouts.getAndAdd(value.toInt())
            BackpropProperty.COMPLETED -> node.completedRollouts.getAndAdd(value.toInt())
            BackpropProperty.PLAYER_ONE_SCORE -> if(node.playerNumber == PlayerNumber.PLAYER_ONE) {
                node.score += value.toDouble()
            }
            BackpropProperty.PLAYER_TWO_SCORE -> if(node.playerNumber == PlayerNumber.PLAYER_TWO) {
                node.score += value.toDouble()
            }
        }

        if(node is MCTSChildNode) {
            backpropagate(node.parent, property, value)
        }
    }

    // calculate which node to move forward on
    private fun getNextNode(node: MCTSTreeNode): MCTSChildNode =
        node.children.firstOrNull { it.completedRollouts.get() == 0 } ?:
        (node.children
            .map { nodeValueFn(it, cParameter) }
            .let { it.indexOf(it.max()) }
            .let { node.children[it] })


    // TODO: rather than saving memory by do-undoing, keep a master copy and just reset to its values?
    private fun rollout(rolloutState: GameState): Pair<Double, Double> {
//        print("starting a rollout")
        while (!rolloutState.gameOver) {
            rolloutState.processEvent(rolloutState.getNextEvent())
        }
        logger.recordPlayout()
        return rolloutScoreFn(rolloutState)
    }

    private fun forward(
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
            is RootNode -> {
                val drawBranch = simState.getNextBranch()
                val sample = MCTSChildNode.getChildren(
                    state = simState,
                    branch = drawBranch,
                    parent = node,
                    actionPolicy = actionPolicy,
                    treasurePolicy = treasurePolicy).single()
                val sampleCards = (sample.selection as DrawSelection).cards.sorted()

                // TODO: clean up
                var alreadySampled = false
                var gameOver = false // TODO: hacky
                for(child in node.children) {
                    if(child.selection != SpecialBranchSelection.GAME_OVER) {
                        val childCards = (child.selection as DrawSelection).cards.sorted()
                        if(childCards == sampleCards) {
                            alreadySampled = true
                            child.score += 1
                        }
                    } else {
                        gameOver = true
                    }
                }

                if(!alreadySampled && !gameOver) {
                    node.children.add(sample)
                    graphPairs.add(Pair(node.id, sample.id))
                }
                simState.eventStack.push(drawBranch)
                forward(simState, getNextNode(node))
            }
            is EndGameNode -> {
                backpropagate(node, BackpropProperty.IN_PROCESS)
                // TODO: make sure that the selection is always the game over selection for this
                rolloutResults.add(
                    RolloutResult(node, rolloutScoreFn(simState))
                )
            }
            is MCTSChildNode -> {
                // TODO: coins is sometimes negative
                val branch = simState.getNextBranch()
                if(branch.context != node.context || simState.currentPlayerNumber != node.playerNumber) {
                    throw IllegalStateException()
                } else { // TODO: somehow,
                    // do the action that the node describes
                    simState.processBranchSelection(node.context, node.selection)
                }

                // TODO: if we're doing, say, a buy, and then drawing, the draw is going to be the same
                //       regardless of the buy.

                val completedRollouts = node.completedRollouts.get()
                if(completedRollouts == 0) {

                    node.children.addAll(
                        MCTSChildNode.getChildren(
                            state = simState,
                            branch = simState.getNextBranch(),
                            parent = node,
                            actionPolicy = actionPolicy,
                            treasurePolicy = treasurePolicy))
                    for(child in node.children) {
                        graphPairs.add(Pair(node.id, child.id))
                    } // TODO: hacky
                    backpropagate(node, BackpropProperty.IN_PROCESS)

                    rolloutResults.add(
                        RolloutResult(node, rollout(simState))
                    )
                } else {
                    val nextBranch = simState.getNextBranch()

                    if(nextBranch.context == BranchContext.DRAW) {

                        val sample = MCTSChildNode.getChildren(
                            state = simState,
                            branch = nextBranch,
                            parent = node,
                            actionPolicy = actionPolicy,
                            treasurePolicy = treasurePolicy).single()
                        val sampleCards = (sample.selection as DrawSelection).cards.sorted()

                        // TODO: clean up
                        var alreadySampled = false
                        var gameOver = false // TODO: hacky
                        for(child in node.children) {
                            if(child.selection != SpecialBranchSelection.GAME_OVER) {
                                val childCards = (child.selection as DrawSelection).cards.sorted()
                                if(childCards == sampleCards) {
                                    alreadySampled = true
                                }
                            } else {
                                gameOver = true
                            }
                        }

                        if(!alreadySampled && !gameOver) {
                            node.children.add(sample)
                            graphPairs.add(Pair(node.id, sample.id))
                        }
                    } else if(node is DrawChildNode && completedRollouts == 1) {
                        node.children.addAll(
                            MCTSChildNode.getChildren(
                                state = simState,
                                branch = nextBranch,
                                parent = node,
                                actionPolicy = actionPolicy,
                                treasurePolicy = treasurePolicy))
                        for(child in node.children) {
                            graphPairs.add(Pair(node.id, child.id))
                        } // TODO: hacky
                    }

                    simState.eventStack.push(nextBranch)

                    forward(simState, getNextNode(node))
                }
            }
            else -> throw IllegalStateException()
        }
    }

    private fun simulationPolicy(state: GameState, branch: Branch): BranchSelection {

        logger.initDecision()
        maxDepth = 0
        maxTurns = 0

        val shuffledState = state.copy(
            newPolicies = listOf(rolloutPolicy, rolloutPolicy),
            newMaxTurns = 999, // TODO: think about this
            newLog = false,
            keepHistory = false,
        )

        root = RootNode.new(
            state = shuffledState,
            branch = branch,
            actionPolicy = actionPolicy,
            treasurePolicy = treasurePolicy,
        )

        for(child in root!!.children) {
            graphPairs.add(Pair(root!!.id, child.id))
        } // TODO: hacky

        var queued = 0
        var processed = 0 // TODO: replace with simulations at root?

        while(processed < rollouts) {
            while(rolloutResults.isNotEmpty()) {
                rolloutResults.remove().let {
                    backpropagate(it.node, BackpropProperty.PLAYER_ONE_SCORE, it.scores.first)
                    backpropagate(it.node, BackpropProperty.PLAYER_TWO_SCORE, it.scores.second)
                    backpropagate(it.node, BackpropProperty.IN_PROCESS, -1)
                    backpropagate(it.node, BackpropProperty.COMPLETED)
                    processed += 1
                }
            }

            if(queued < rollouts) {
                executor.execute {
                    forward(
                        node = root!!,
                        simState = shuffledState.copy(
                            newMaxTurns = max(50, maxTurns + 10)
                        )
                    ) }
                queued += 1
            }
        }

        // the first node will always be the redraw of the opponent's hand

        val opponentDraw = root!!.children.maxBy { it.completedRollouts.get() }

        // TODO: simplify
        val selection = opponentDraw.children.maxBy { it.completedRollouts.get() }.selection

        return selection
             .also { logger.recordDecision(contextMap, maxDepth, maxTurns - state.turns) }
    }

    override fun policy(
        state: GameState,
        branch: Branch
    ): BranchSelection {

        return when(branch.context) {
            BranchContext.GAME_OVER -> SpecialBranchSelection.GAME_OVER
            BranchContext.DRAW -> drawPolicy(state, branch)
            BranchContext.CHOOSE_ACTION -> {
                if(!state.currentPlayer.visibleHand) {
                    throw IllegalStateException()
                }
                val hand = state.currentPlayer.knownHand.toList()
                if(hand.firstOrNull { it.addActions > 0 } != null || hand.filter { it.type == CardType.ACTION }.size < 2) {
                    actionPolicy(state, branch)
                } else {
                    simulationPolicy(state, branch)
                }
            }
            BranchContext.CHOOSE_TREASURE -> treasurePolicy(state, branch)
            else -> simulationPolicy(state, branch)
        }
    }

    override fun finally() {
        // TODO: clear anything that can no longer be reached
    }

}