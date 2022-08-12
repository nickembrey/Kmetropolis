package policies.mcts

import engine.EventStack
import engine.GameState
import engine.branch.BranchContext
import engine.branch.BranchSelection
import engine.operation.HistoryOperation
import engine.player.PlayerNumber
import java.util.concurrent.atomic.AtomicInteger
import kotlin.properties.Delegates

// TODO: reconsider how we're using wins vs score and optional PolicyPlayer

// a child node that represents a game decision
class DecisionChildNode constructor(
    parent: MCTSTreeNode,
    history: MutableList<HistoryOperation>,
    eventStack: EventStack,
    selections: List<BranchSelection>,
    playerNumber: PlayerNumber,
    turns: Int,
    context: BranchContext,
    override val weight: Double = 1.0 // TODO:
): MCTSChildNode(parent, history, eventStack, selections, playerNumber, turns, context) {
    override var score: Double = 0.0
}