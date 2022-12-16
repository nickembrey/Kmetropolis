package policies.heuristic

import engine.*
import engine.branch.*
import engine.card.Card
import policies.Policy
import policies.PolicyName
import policies.delegates.draw.RandomDrawPolicy
import policies.mcts.rollout.RandomPolicy

class SingleWitchV3Policy : Policy() {

    private val randomDrawPolicy = RandomDrawPolicy()

    override val name = PolicyName("singleWitchV3Policy")
    override fun finally() = Unit
    override fun policy(
        state: GameState,
        branch: Branch
    ): BranchSelection {

        val options = branch.getOptions(state)

        return when(branch.context) {
            BranchContext.DRAW -> randomDrawPolicy(state, branch)
            BranchContext.CHOOSE_ACTION -> options.firstOrNull { it is ActionSelection } ?: options.first()
            BranchContext.CHOOSE_TREASURE -> options.firstOrNull { it is TreasureSelection } ?: options.first()
            BranchContext.CHOOSE_BUY -> {
                val goldCards: Int = state.currentPlayer.allCards[Card.GOLD]
                val witchCards = state.currentPlayer.allCards[Card.WITCH]

                val witchLeft = state.board[Card.WITCH]

                return if(state.currentPlayer.coins >= 8 && goldCards > 0) {
                    BuySelection(cards = listOf(Card.PROVINCE))
                } else if (state.currentPlayer.coins >= 5 && witchCards == 0 && witchLeft > 0) {
                    BuySelection(cards = listOf(Card.WITCH))
                } else if (state.currentPlayer.coins >= 6) {
                    BuySelection(cards = listOf(Card.GOLD))
                } else if (state.currentPlayer.coins >= 3) {
                    BuySelection(cards = listOf(Card.SILVER))
                } else {
                    SpecialBranchSelection.SKIP
                }
            }
            BranchContext.MILITIA -> options.random()
            else -> RandomPolicy()(state, branch)
        }
    }
}

