package policies.utility

import engine.*
import engine.card.Card
import engine.branch.BranchSelection
import engine.branch.BranchContext
import engine.branch.SpecialBranchSelection
import engine.card.CardType
import policies.Policy
import policies.PolicyName

class PlayAllTreasuresPolicy: Policy() {
    override val name = PolicyName("playAllTreasuresPolicy")
    override fun shutdown() = Unit
    override fun policy(state: GameState): BranchSelection {
        return if (state.context == BranchContext.CHOOSE_TREASURE) {
            val options = state.context.toOptions(state)
            options.firstOrNull { it is Card && it.type == CardType.TREASURE } ?: SpecialBranchSelection.SKIP
        } else {
            throw IllegalArgumentException()
        }
    }
}

