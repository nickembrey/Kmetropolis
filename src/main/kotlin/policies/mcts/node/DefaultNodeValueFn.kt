package policies.mcts.node

import kotlin.math.ln
import kotlin.math.sqrt

object DefaultNodeValueFn: NodeValueFn {

    override fun invoke(node: MCTSChildNode, cParameter: Double): Double {
        return (node.score / node.completedRollouts.get()).let {
            it + // variance term
                    (cParameter * sqrt(
                        ln(node.parent.completedRollouts.toDouble() + node.parent.currentRollouts.toDouble()) /
                                (node.completedRollouts.toDouble() + node.currentRollouts.toDouble())
                    ))
        }
    }
}