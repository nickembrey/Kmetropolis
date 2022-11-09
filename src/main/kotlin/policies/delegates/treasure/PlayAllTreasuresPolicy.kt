package policies.delegates.treasure

import engine.*
import engine.branch.*
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
        return if (branch.context == BranchContext.CHOOSE_TREASURE && state.currentPlayer.visibleHand) {
            val treasures = state.currentPlayer.knownHand.toList().filter { it.type == CardType.TREASURE }
            when(treasures.size) {
                0 -> SpecialBranchSelection.SKIP
                else -> TreasureSelection(cards = treasures)
            }
        } else {
            throw IllegalArgumentException()
        }
    }
}

