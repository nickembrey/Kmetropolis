package policies.delegates.draw

import engine.*
import engine.branch.Branch
import engine.branch.BranchContext
import engine.branch.BranchSelection
import engine.branch.DrawSelection
import policies.Policy
import policies.PolicyName
import kotlin.random.Random

class RandomDrawPolicy : Policy() {
    override val name = PolicyName("randomPolicy")
    override fun shutdown() = Unit
    override fun policy(
        state: GameState,
        branch: Branch
    ): BranchSelection {
        if(state.context != BranchContext.DRAW) {
            throw IllegalStateException()
        }
        val options = branch.getOptions(state) as List<DrawSelection> // TODO:
        if(options.isNotEmpty()) {
            var random = Random.nextDouble(0.0, options.sumOf { it.probability })
            for(option in options) {
                random -= option.probability
                if(random <= 0.0) return option
            }
        } else {
            throw IllegalStateException()
        }

        throw IllegalStateException() // TODO: hacky
    }
}