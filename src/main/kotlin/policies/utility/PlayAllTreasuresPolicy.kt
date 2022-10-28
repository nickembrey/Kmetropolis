package policies.utility

import engine.*
import engine.branch.*
import engine.card.Card
import engine.card.CardType
import policies.Policy
import policies.PolicyName

class PlayAllTreasuresPolicy: Policy() { // TODO: require that all major policies implement minor policies associated with each phase
    override val name = PolicyName("playAllTreasuresPolicy")
    override fun finally() = Unit
    override fun policy(
        state: GameState,
        branch: Branch
    ): BranchSelection {
        return if (branch.context == BranchContext.CHOOSE_TREASURE) {
            val options = branch.getOptions(state)
            options.firstOrNull { it is TreasureSelection } ?: SpecialBranchSelection.SKIP
        } else {
            throw IllegalArgumentException()
        }
    }
}

