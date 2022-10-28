package policies.mcts.node

import engine.branch.BranchContext
import engine.branch.BranchSelection
import engine.player.PlayerNumber
import java.util.concurrent.atomic.AtomicInteger

// TODO: dosn't need history or event stack

// a child node that represents a draw selection
class DrawChildNode constructor( // TODO: somehow make this only available in MCTSChildNode?
    parent: MCTSTreeNode,
    selection: BranchSelection,
    playerNumber: PlayerNumber,
    turns: Int,
    context: BranchContext,
    val probability: Double
): MCTSChildNode(
    parent = parent,
    selection = selection,
    playerNumber = playerNumber,
    turns = turns,
    context = context,
    completedRollouts = AtomicInteger(1)
) {
    override var score: Double = 0.0
}