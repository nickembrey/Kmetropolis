package policies.mcts

import engine.EventStack
import engine.GameState
import engine.branch.BranchContext
import engine.branch.BranchSelection
import engine.operation.HistoryOperation
import engine.player.PlayerNumber
import logger
import ml.WeightSource
import java.util.concurrent.atomic.AtomicInteger
import kotlin.properties.Delegates

// TODO: consider adding LeafNode that will be the only thing to have the command history
abstract class MCTSChildNode protected constructor(
    val parent: MCTSTreeNode,
    val history: MutableList<HistoryOperation>,
    val eventStack: EventStack,
    val selection: BranchSelection, // TODO: replace with commands
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
    var index by Delegates.notNull<Int>() // an index for keeping track of the node in some collection

    override var currentRollouts: AtomicInteger = AtomicInteger(0)
    override var completedRollouts: AtomicInteger = AtomicInteger(0)

    companion object {
        fun getChildren(
            state: GameState,
            parent: MCTSTreeNode,
            history: MutableList<HistoryOperation>,
            eventStack: EventStack,
            selections: Collection<BranchSelection>,
            weightSource: WeightSource? = null // TODO: use a default weight source that gives everything 1
        ): List<MCTSChildNode> = when(state.context) {
            BranchContext.DRAW -> {
                // TODO: let this handle getting the selections, since we want a different method for drawing rather
                //       than toOptions
                val probabilities = state.currentPlayer.getDrawProbabilities()
                probabilities.map {
                    DrawChildNode(
                        parent = parent,
                        history = history,
                        eventStack = eventStack,
                        selection = it.key,
                        playerNumber = state.currentPlayer.playerNumber,
                        turns = state.turns,
                        context = state.context,
                        weight = it.value
                    )
                }
            }
            else -> {
                val weights = weightSource?.getWeights(state, selections)
                if(weights != null) {
                    logger.logWeightUse()
                }

                selections.mapIndexed { index, it ->

                    DecisionChildNode( // TODO: not necessarily Decision
                        parent = parent,
                        history = history,
                        eventStack = eventStack,
                        selection = it,
                        playerNumber = state.currentPlayer.playerNumber,
                        turns = state.turns,
                        context = state.context,
                        weight = if(weights != null) weights[index] else 1.0
                    )
                }
            }



        }
    }
}