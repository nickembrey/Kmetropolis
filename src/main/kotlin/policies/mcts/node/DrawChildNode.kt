package policies.mcts.node

import engine.branch.BranchContext
import engine.branch.BranchSelection
import engine.player.PlayerNumber
import java.util.concurrent.atomic.AtomicInteger
// a child node that represents a draw selection
class DrawChildNode constructor(
    parent: MCTSTreeNode,
    selection: BranchSelection,
    playerNumber: PlayerNumber,
    turns: Int,
    context: BranchContext,
    id: Int
): MCTSChildNode(
    parent = parent,
    selection = selection,
    playerNumber = playerNumber,
    turns = turns,
    context = context,
    completedRollouts = AtomicInteger(1),
    id = id
) {
    override var score: Double = 0.0
}