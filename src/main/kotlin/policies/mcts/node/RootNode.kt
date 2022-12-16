package policies.mcts.node

import engine.GameState
import engine.branch.Branch
import engine.player.PlayerNumber
import policies.Policy
import java.util.concurrent.atomic.AtomicInteger

class RootNode private constructor(
    override val playerNumber: PlayerNumber,
    override val turns: Int
): MCTSTreeNode {

    override val id: Int = 0

    override val rootPlayerNumber: PlayerNumber = playerNumber

    override var score: Double = 0.0

    override var children: MutableList<MCTSChildNode> = mutableListOf()

    override val depth: Int = 0

    override var currentRollouts: AtomicInteger = AtomicInteger(0)
    override var completedRollouts: AtomicInteger = AtomicInteger(0)

    companion object {
        /**
         * Note that the root is different from all the other nodes because it represents the context from which all
         * other branches are made from, whereas all other nodes represent branch selections and are never contexts.
         */
        fun new(
            state: GameState,
            branch: Branch,
            actionPolicy: Policy,
            treasurePolicy: Policy,
        ): RootNode {

            // push the branch back on
            state.eventStack.push(branch)

            // simulate a redraw of the opponent's hand
            state.opponentRedraw()

            return RootNode(state.currentPlayerNumber, state.turns)
                .apply {

                    val drawBranch = state.getNextBranch()

                    val newChildren = MCTSChildNode.getChildren(
                        state = state,
                        branch = drawBranch,
                        parent = this,
                        actionPolicy = actionPolicy,
                        treasurePolicy = treasurePolicy)

                    children.addAll(newChildren)

                    state.eventStack.push(drawBranch)

                } }
    }
}