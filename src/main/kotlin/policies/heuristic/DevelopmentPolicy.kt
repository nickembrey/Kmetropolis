package policies.heuristic

import engine.*
import engine.branch.*
import engine.card.Card
import engine.card.CardType
import policies.Policy
import policies.PolicyName
import policies.delegates.action.MPPAFPolicy
import policies.jansen_tollisen.HeuristicGreedyPolicy
import policies.rollout.RandomPolicy

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
                val buySelections = options // TODO: buying weird stuff bc of the aggergation change, probably happening elsewhere too
                    .asSequence()
                    .filterIsInstance<BuySelection>()
                    .filter { !it.cards.contains(Card.CURSE) && !it.cards.contains(Card.COPPER) && !it.cards.contains(Card.ESTATE) }
                    .filter { !(it.cards.contains(Card.DUCHY) && state.board.count { pile -> pile.value == 0 } == 0) }
                    .filter { !(it.cards.contains(Card.GARDENS) && state.currentPlayer.allCards.size < 30) }
                    .filter { !(actionDensity < 0.1 && it.cards.any { card -> card.type == CardType.ACTION } && !it.cards.contains(Card.WITCH) && it.cards.any { card -> card.addActions == 0} ) }
                    .filter { !(actionDensity > 0.4 && it.cards.any { card -> card.type == CardType.ACTION } && it.cards.any { card -> card.addActions == 0})}
                    .filter { !(state.currentPlayer.allCards.count { card -> card == Card.WITCH } >= 2 && it.cards.contains(Card.WITCH) ) }
                    .sortedWith(compareByDescending { it.cards.contains(Card.GOLD) })
                    .sortedWith(compareByDescending { it.cards.sumOf { card -> card.cost } })
                    .sortedWith(compareByDescending { it.cards.any { card -> card.type == CardType.VICTORY } })
                    .sortedWith(compareByDescending { it.cards.contains(Card.WITCH) })
                    .toList()
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
