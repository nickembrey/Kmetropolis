package policies.mcts.node

import engine.EventStack
import engine.branch.BranchContext
import engine.branch.BranchSelection
import engine.operation.HistoryOperation
import engine.player.PlayerNumber
import java.util.concurrent.atomic.AtomicInteger

// TODO: reconsider how we're using wins vs score and optional PolicyPlayer

// a child node that represents a game decision
class DecisionChildNode constructor(
    parent: MCTSTreeNode,
    selection: BranchSelection,
    playerNumber: PlayerNumber,
    turns: Int,
    context: BranchContext,
    id: Int
): MCTSChildNode(parent, selection, playerNumber, turns, context, AtomicInteger(0), id) {
    override var score: Double = 0.0
}