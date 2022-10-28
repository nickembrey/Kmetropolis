package policies.mcts

import policies.mcts.node.MCTSTreeNode

data class RolloutResult(
    val node: MCTSTreeNode,
    val scores: Pair<Double, Double>
)