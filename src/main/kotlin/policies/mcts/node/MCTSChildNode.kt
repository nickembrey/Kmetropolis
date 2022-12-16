package policies.mcts.node

import engine.GameState
import engine.branch.*
import engine.player.PlayerNumber
import policies.Policy
import java.util.concurrent.atomic.AtomicInteger

abstract class MCTSChildNode protected constructor(
    val parent: MCTSTreeNode,
    val selection: BranchSelection,
    override val playerNumber: PlayerNumber,
    override val turns: Int,
    val context: BranchContext,
    override var completedRollouts: AtomicInteger,
    override val id: Int
): MCTSTreeNode {

    override val rootPlayerNumber = parent.rootPlayerNumber
    override val depth: Int = parent.depth + 1

    abstract override var score: Double

    override var children: MutableList<MCTSChildNode> = mutableListOf()

    override var currentRollouts: AtomicInteger = AtomicInteger(0)

    companion object {

        val nextId: AtomicInteger = AtomicInteger(1)

        fun getChildren(
            state: GameState,
            branch: Branch,
            parent: MCTSTreeNode,
            actionPolicy: Policy,
            treasurePolicy: Policy
        ): List<MCTSChildNode> {

            val selections = when (branch.context) {
                BranchContext.CHOOSE_ACTION -> listOf(actionPolicy(state, branch))
                BranchContext.CHOOSE_TREASURE -> listOf(treasurePolicy(state, branch))
                else -> branch.getOptions(state)
            }

            return when(val context = branch.context) {
                BranchContext.DRAW -> {

                    selections.map {

                        if(it !is VisibleDrawSelection) {
                            throw IllegalStateException()
                        }

                        DrawChildNode(
                            parent = parent,
                            selection = it,
                            playerNumber = state.currentPlayer.playerNumber,
                            turns = state.turns,
                            context = context,
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

                        DecisionChildNode(
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