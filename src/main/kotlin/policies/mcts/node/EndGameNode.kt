package policies.mcts.node

import engine.branch.BranchContext
import engine.branch.BranchSelection
import engine.player.PlayerNumber
import java.util.concurrent.atomic.AtomicInteger

class EndGameNode constructor(
    parent: MCTSTreeNode,
    selection: BranchSelection,
    playerNumber: PlayerNumber,
    turns: Int,
    context: BranchContext,
    id: Int
): MCTSChildNode(parent, selection, playerNumber, turns, context, AtomicInteger(0), id) {
    override var score: Double = 0.0
}