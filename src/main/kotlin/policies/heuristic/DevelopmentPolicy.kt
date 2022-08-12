package policies.heuristic

import engine.*
import engine.branch.Branch
import engine.branch.BranchContext
import engine.branch.BranchSelection
import engine.branch.SpecialBranchSelection
import engine.card.Card
import engine.card.CardType
import policies.Policy
import policies.PolicyName
import policies.delegates.action.MPPAFPolicy
import policies.delegates.draw.RandomDrawPolicy
import policies.jansen_tollisen.HeuristicGreedyPolicy
import policies.utility.RandomPolicy

class DevelopmentPolicy : Policy() {

    companion object {
        val randomPolicy = RandomPolicy()
    }

    override val name: PolicyName = PolicyName("DevelopmentPolicy")
    override fun shutdown() = Unit
    override fun policy(
        state: GameState,
        branch: Branch
    ): BranchSelection {
        val options = branch.getOptions(state)
        return when (state.context) {
            BranchContext.CHOOSE_ACTION -> {
                MPPAFPolicy().policy(state, branch)
            }
            BranchContext.CHOOSE_TREASURE -> {
                return options.firstOrNull { it is Card } ?: SpecialBranchSelection.SKIP
            }
            BranchContext.CHOOSE_BUYS -> {

                val actionDensity = state.currentPlayer.allCards.sumOf { it.addActions } / state.currentPlayer.allCards.size.toDouble()

                // TODO: weird corner case where all coppers and all curses are gone?
                val buySelections = options
                    .filterIsInstance<Card>()
                    .filter { it != Card.CURSE && it != Card.COPPER && it != Card.ESTATE }
                    .filter { !(it == Card.DUCHY && state.board.count { pile -> pile.value == 0 } == 0) }
                    .filter { !(it == Card.GARDENS && state.currentPlayer.allCards.size < 30) }
                    .filter { !(actionDensity < 0.1 && it.type == CardType.ACTION && it != Card.WITCH && it.addActions == 0) }
                    .filter { !(actionDensity > 0.4 && it.type == CardType.ACTION && it.addActions > 1)}
                    .filter { !(state.currentPlayer.allCards.count { card -> card == Card.WITCH } >= 2 && it == Card.WITCH ) }
                    .sortedWith(compareByDescending { it.type == CardType.TREASURE })
                    .sortedWith(compareByDescending { it.cost })
                    .sortedWith(compareByDescending {it.type == CardType.VICTORY })
                    .sortedWith(compareByDescending { it == Card.WITCH })
                return if (buySelections.isNotEmpty()) {
                    buySelections[0]
                } else {
                    SpecialBranchSelection.SKIP
                }
            }
            else -> HeuristicGreedyPolicy.randomPolicy.policy(state, branch)

        }
    }
}
