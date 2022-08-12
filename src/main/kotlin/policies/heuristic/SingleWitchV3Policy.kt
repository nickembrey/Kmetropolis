package policies.heuristic

import engine.*
import engine.branch.Branch
import engine.card.Card
import engine.branch.BranchSelection
import engine.branch.BranchContext
import engine.branch.SpecialBranchSelection
import policies.Policy
import policies.PolicyName
import policies.delegates.draw.RandomDrawPolicy
import policies.utility.RandomPolicy

class SingleWitchV3Policy : Policy() { // TODO: abstract witch policy

    private val randomDrawPolicy = RandomDrawPolicy()

    override val name = PolicyName("singleWitchV3Policy")
    override fun shutdown() = Unit
    override fun policy(
        state: GameState,
        branch: Branch
    ): BranchSelection {

        val options = branch.getOptions(state)

        return when(state.context) {
            BranchContext.DRAW -> randomDrawPolicy.policy(state, branch)
            BranchContext.CHOOSE_ACTION -> options.firstOrNull { it is Card } ?: options.first()
            BranchContext.CHOOSE_TREASURE -> options.firstOrNull { it is Card } ?: options.first()
            BranchContext.CHOOSE_BUYS -> {
                val goldCards: Int = state.currentPlayer.allCards.filter { it == Card.GOLD }.size
                val witchCards = state.currentPlayer.allCards.filter { it == Card.WITCH }.size

                val witchLeft = state.board[Card.WITCH]!!

                return if(state.currentPlayer.coins >= 8 && goldCards > 0) {
                    Card.PROVINCE
                } else if (state.currentPlayer.coins >= 5 && witchCards == 0 && witchLeft > 0) {
                    Card.WITCH
                } else if (state.currentPlayer.coins >= 6) {
                    Card.GOLD
                } else if (state.currentPlayer.coins >= 3) {
                    Card.SILVER
                } else {
                    SpecialBranchSelection.SKIP
                }
            }
            BranchContext.MILITIA -> options.random()
            else -> RandomPolicy().policy(state, branch)
        }
    }
}

