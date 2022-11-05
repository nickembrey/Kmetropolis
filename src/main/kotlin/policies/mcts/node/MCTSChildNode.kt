package policies.mcts.node

import engine.EventStack
import engine.GameState
import engine.branch.*
import engine.operation.HistoryOperation
import engine.player.PlayerNumber
import policies.Policy
import java.util.concurrent.atomic.AtomicInteger

// TODO: consider adding LeafNode that will be the only thing to have the command history
abstract class MCTSChildNode protected constructor(
    val parent: MCTSTreeNode,
    val selection: BranchSelection, // TODO: replace with commands
    override val playerNumber: PlayerNumber,
    override val turns: Int,
    val context: BranchContext,
    override var completedRollouts: AtomicInteger,
    override val id: Int
): MCTSTreeNode {

    override val rootPlayerNumber = parent.rootPlayerNumber // TODO: space or time?
    override val depth: Int = parent.depth + 1

    abstract override var score: Double

    // TODO: does this need to be made concurrency safe?
    // TODO: only allow to be set once
    override var children: MutableList<MCTSChildNode> = mutableListOf()

    override var currentRollouts: AtomicInteger = AtomicInteger(0)

    companion object {

        val nextId: AtomicInteger = AtomicInteger(1)

        fun getChildren( // TODO: shouldn't this be a method of MCTSTreeNode?
            state: GameState,
            branch: Branch,
            parent: MCTSTreeNode,
            actionPolicy: Policy,
            treasurePolicy: Policy
        ): List<MCTSChildNode> {

            // TODO: audit the results

            val selections = when (branch.context) {
                BranchContext.CHOOSE_ACTION -> listOf(actionPolicy(state, branch))
                BranchContext.CHOOSE_TREASURE -> listOf(treasurePolicy(state, branch))
                else -> branch.getOptions(state)
            }

            return when(val context = branch.context) {
                BranchContext.DRAW -> { // TODO: unify with below

                    selections.mapIndexed { index, it ->

                        if(it !is DrawSelection) { // TODO: hacky
                            throw IllegalStateException()
                        }

                        DrawChildNode(
                            parent = parent,
                            selection = it, // TODO: do we need the list?
                            playerNumber = state.currentPlayer.playerNumber,
                            turns = state.turns,
                            context = context,
                            probability = it.probability,
                            id = nextId.getAndIncrement()
                        )
                    }
                }
                BranchContext.GAME_OVER -> {

                    listOf(
                        EndGameNode(
                            parent = parent,
                            selection = SpecialBranchSelection.GAME_OVER,
                            playerNumber = state.currentPlayer.playerNumber,
                            turns = state.turns,
                            context = context,
                            id = nextId.getAndIncrement()
                        )
                    )
                }
                else -> {

                    selections.map {

                        DecisionChildNode( // TODO: not necessarily Decision
                            parent = parent,
                            selection = it,
                            playerNumber = state.currentPlayer.playerNumber,
                            turns = state.turns,
                            context = context,
                            id = nextId.getAndIncrement()
                        )
                    }
                }



            }
        }
    }
}