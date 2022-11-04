package policies.mcts.node

import engine.player.PlayerNumber
import policies.mcts.node.MCTSChildNode
import java.util.concurrent.atomic.AtomicInteger

interface MCTSTreeNode {

    val id: Int

    val rootPlayerNumber: PlayerNumber
    val playerNumber: PlayerNumber // the player associated with the node for scoring

    var score: Double // the original score, e.g., number of wins or vp difference

    val children: List<MCTSChildNode>

    val depth: Int
    val turns: Int

    // TODO: make it optional to have concurrency, we can use cheaper types if not

    // TODO: these can be vals, no?
    // TODO: rename, currentRollouts and completedRollouts

    // TODO: why is currentRollouts negative at decision time?
    var currentRollouts: AtomicInteger // number of rollouts in process by threads
    var completedRollouts: AtomicInteger // number of rollouts completed
}