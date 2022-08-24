package policies.mcts

import engine.EventStack
import engine.GameState
import engine.branch.BranchContext
import engine.branch.BranchSelection
import engine.branch.DrawSelection
import engine.branch.SpecialBranchSelection
import engine.card.CardType
import engine.operation.HistoryOperation
import engine.player.PlayerNumber
import logger
import ml.WeightSource
import policies.Policy
import java.util.concurrent.atomic.AtomicInteger
import kotlin.properties.Delegates

// TODO: consider adding LeafNode that will be the only thing to have the command history
abstract class MCTSChildNode protected constructor(
    val parent: MCTSTreeNode,
    val history: MutableList<HistoryOperation>,
    val eventStack: EventStack,
    val selections: List<BranchSelection>, // TODO: replace with commands
    override val playerNumber: PlayerNumber,
    override val turns: Int,
    val context: BranchContext,
): MCTSTreeNode {

    override val rootPlayerNumber = parent.rootPlayerNumber // TODO: space or time?
    override val depth: Int = parent.depth + 1

    abstract override var score: Double

    // TODO: does this need to be made concurrency safe?
    // TODO: only allow to be set once
    override var children: MutableList<MCTSChildNode> = mutableListOf()

    // TODO: HashMap entry on creation, or even just a tree
    var index: Int? = null // an index for keeping track of the node in some collection

    override var currentRollouts: AtomicInteger = AtomicInteger(0)
    override var completedRollouts: AtomicInteger = AtomicInteger(0)

    companion object {

        // TODO: this should be put in MCTSChildNode
        protected fun getNextOptions(
            simState: GameState,
            parent: MCTSTreeNode,
            actionPolicy: Policy,
            treasurePolicy: Policy,
        ): Collection<BranchSelection> {
            if(parent is MCTSChildNode) {
                for(selection in parent.selections) {
                    simState.processBranchSelection(parent.context, selection) // put the action on the stack
                }
            }
            var nextBranch = simState.getNextBranch()
            var nextOptions = nextBranch.getOptions(simState, aggregated = true)
            while(true) { // TODO: hacky
                if(nextOptions.isEmpty()) {
                    throw IllegalStateException()
                } else if (nextOptions.size == 1) { // TODO
                    if(nextOptions.single() == SpecialBranchSelection.GAME_OVER) {
                        return nextOptions
                    } else {
                        simState.processBranchSelection(nextBranch.context, nextOptions.single())
                        nextBranch = simState.getNextBranch()
                        nextOptions = nextBranch.getOptions(simState, aggregated = true)
                    }
                } else when (nextBranch.context) {
                    BranchContext.CHOOSE_ACTION -> {
                        if(simState.currentPlayer.hand.firstOrNull { it.addActions > 0 } != null || simState.currentPlayer.hand.filter { it.type == CardType.ACTION }.size < 2) {
                            val selection = actionPolicy.policy(simState, nextBranch)
                            simState.processBranchSelection(nextBranch.context, selection)
                            nextBranch = simState.getNextBranch()
                            nextOptions = nextBranch.getOptions(simState, aggregated = true)
                        } else {
                            return nextOptions
                        }
                    }
                    BranchContext.CHOOSE_TREASURE -> {
                        val selection = treasurePolicy.policy(simState, nextBranch)
                        simState.processBranchSelection(nextBranch.context, selection)
                        nextBranch = simState.getNextBranch()
                        nextOptions = nextBranch.getOptions(simState, aggregated = true)
                    }
                    else -> return nextOptions
//                .also { simState.eventStack.push(nextContext)} // so the rollout doesn't skip the decision
                }
            }

        }
        fun getChildren(
            state: GameState,
            parent: MCTSTreeNode,
            actionPolicy: Policy,
            treasurePolicy: Policy,
            selections: Collection<BranchSelection> = getNextOptions(
                state, parent, actionPolicy, treasurePolicy
            ),
            weightSource: WeightSource? = null // TODO: use a default weight source that gives everything 1
        ): List<MCTSChildNode> = when(state.context) {
            BranchContext.DRAW -> { // TODO: unify with below

                val eventStack = state.eventStack.copy()
                val operationHistory = state.operationHistory.toMutableList()

                selections.map {

                    if(it !is DrawSelection) { // TODO: hacky
                        throw IllegalStateException()
                    }

                    DrawChildNode(
                        parent = parent,
                        history = if(parent is RootNode) {
                            mutableListOf()
                        } else {
                            operationHistory
                        },
                        eventStack = eventStack,
                        selections = listOf(it), // TODO: do we need the list?
                        playerNumber = state.currentPlayer.playerNumber,
                        turns = state.turns,
                        context = state.context,
                        weight = it.probability
                    )
                }
            }
            BranchContext.GAME_OVER -> {

                val eventStack = state.eventStack.copy() // TODO: KISS

                listOf(LeafChildNode(
                    parent = parent,
                    history = mutableListOf(),
                    eventStack = eventStack,
                    selections = listOf(),
                    playerNumber = state.currentPlayer.playerNumber,
                    turns = state.turns,
                    context = state.context,
                    weight = 1.0
                ))
            }
            else -> {

                val eventStack = state.eventStack.copy()
                val operationHistory = state.operationHistory.toMutableList()

                selections.mapIndexed { index, it ->

                    DecisionChildNode( // TODO: not necessarily Decision
                        parent = parent,
                        history = if(parent is RootNode) {
                            mutableListOf()
                        } else {
                            operationHistory
                        },
                        eventStack = eventStack,
                        selections = listOf(it),
                        playerNumber = state.currentPlayer.playerNumber,
                        turns = state.turns,
                        context = state.context
                    )
                }
            }



        }
    }
}