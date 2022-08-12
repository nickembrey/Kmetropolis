package policies.mcts

import engine.GameState
import engine.branch.Branch
import engine.player.PlayerNumber
import ml.WeightSource
import policies.Policy
import java.util.concurrent.atomic.AtomicInteger

class RootNode private constructor(
    override val playerNumber: PlayerNumber,
    override val turns: Int
): MCTSTreeNode {

    override val rootPlayerNumber: PlayerNumber = playerNumber

    override var score: Double = 0.0
    override val weight: Double = 1.0

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
            weightSource: WeightSource? = null): RootNode {
            return RootNode(state.currentPlayerNumber, state.turns)
                .apply {
                    children.addAll(
                        MCTSChildNode.getChildren(
                            state = state,
                            parent = this,
                            actionPolicy = actionPolicy,
                            treasurePolicy = treasurePolicy,
                            selections = branch.getOptions(state, groupDraws = true),
                            weightSource = weightSource
                        )
                    ) }
        }
    }
}