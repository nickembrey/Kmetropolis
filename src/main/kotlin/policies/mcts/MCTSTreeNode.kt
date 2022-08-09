package policies.mcts

import engine.GameState
import engine.branch.BranchSelection
import engine.player.PlayerNumber
import java.util.concurrent.atomic.AtomicInteger

interface MCTSTreeNode {

    val rootPlayerNumber: PlayerNumber
    val playerNumber: PlayerNumber // the player associated with the node for scoring

    var score: Double // the original score, e.g., number of wins or vp difference
    val weight: Double // a weight that affects how often the node is selected, e.g., draw probability or ml result

    val children: List<MCTSChildNode>

    val depth: Int
    val turns: Int

    // TODO: make it optional to have concurrency, we can use cheaper types if not

    // TODO: these can be vals, no?
    // TODO: rename, currentRollouts and completedRollouts
    var currentRollouts: AtomicInteger // number of rollouts in process by threads
    var completedRollouts: AtomicInteger // number of rollouts completed
}