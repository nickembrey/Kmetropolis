package policies.delegates.draw

import engine.*
import engine.branch.BranchContext
import engine.branch.BranchSelection
import policies.Policy
import policies.PolicyName

class RandomDrawPolicy : Policy() {
    override val name = PolicyName("randomPolicy")
    override fun shutdown() = Unit
    override fun policy(state: GameState): BranchSelection {
        assert(state.context == BranchContext.DRAW)
        val options = state.context.toOptions(state)
        val random = state.currentPlayer.randomFromDeck()
        assert(random in options)
        return random
    }
}