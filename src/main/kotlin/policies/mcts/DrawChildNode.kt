package policies.mcts

import engine.EventStack
import engine.branch.BranchContext
import engine.branch.BranchSelection
import engine.operation.HistoryOperation
import engine.player.PlayerNumber

// a child node that represents a draw selection
class DrawChildNode constructor( // TODO: somehow make this only available in MCTSChildNode?
    parent: MCTSTreeNode,
    history: MutableList<HistoryOperation>,
    eventStack: EventStack,
    selection: BranchSelection,
    playerNumber: PlayerNumber,
    turns: Int,
    context: BranchContext,
    override val weight: Double
): MCTSChildNode(parent, history, eventStack, selection, playerNumber, turns, context) {
    override var score: Double = 0.0
}