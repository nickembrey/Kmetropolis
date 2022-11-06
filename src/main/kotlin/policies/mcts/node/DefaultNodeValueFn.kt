package policies.mcts.node

import engine.GameState
import engine.player.PlayerNumber
import kotlin.math.ln
import kotlin.math.sqrt
import kotlin.random.Random

object DefaultNodeValueFn: NodeValueFn {

    override fun invoke(node: MCTSChildNode, cParameter: Double): Double {
        return (node.score / node.completedRollouts.get()).let {
            it + // variance term
                    (cParameter * sqrt( // TODO: review variance term
                        ln(node.parent.completedRollouts.toDouble() + node.parent.currentRollouts.toDouble()) /
                                (node.completedRollouts.toDouble() + node.currentRollouts.toDouble())
                    ))
        }
    }
}