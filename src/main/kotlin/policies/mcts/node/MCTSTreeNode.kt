package policies.mcts.node

import engine.player.PlayerNumber
import java.util.concurrent.atomic.AtomicInteger

interface MCTSTreeNode {

    val id: Int

    val rootPlayerNumber: PlayerNumber
    val playerNumber: PlayerNumber // the player associated with the node for scoring

    var score: Double // the original score, e.g., number of wins or vp difference

    val children: List<MCTSChildNode>

    val depth: Int
    val turns: Int

    var currentRollouts: AtomicInteger // number of rollouts in process by threads
    var completedRollouts: AtomicInteger // number of rollouts completed
}