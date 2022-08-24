package policies.rollout

import engine.*
import engine.branch.*
import engine.card.Card
import policies.Policy
import policies.PolicyName
import policies.delegates.draw.RandomDrawPolicy

class GreenRolloutPolicy : Policy() {

    companion object {
        val randomPolicy = RandomPolicy()
        val randomDrawPolicy = RandomDrawPolicy()
    }

    private val cardMenu: ArrayList<Card> = ArrayList(20)
    override val name = PolicyName("greenRolloutPolicy")
    override fun shutdown() = Unit
    override fun policy(
        state: GameState,
        branch: Branch
    ): BranchSelection {

        val options = branch.getOptions(state)

        return when(state.context) {
            BranchContext.DRAW -> randomDrawPolicy.policy(state, branch)
            BranchContext.CHOOSE_ACTION -> {
                cardMenu.clear()
                // order by number of actions first, then by cost
                options
                    .filterIsInstanceTo(cardMenu)
                    .sortWith(compareByDescending<Card> { it.addActions }.thenByDescending { it.cost })
                if(cardMenu.isNotEmpty()) {
                    return cardMenu[0]
                } else {
                    SpecialBranchSelection.SKIP
                }
            }
            BranchContext.CHOOSE_TREASURE -> {
                return options.firstOrNull { it is Card } ?: SpecialBranchSelection.SKIP
            }
            BranchContext.CHOOSE_BUYS -> {
                return options
                    .filterIsInstance<BuySelection>()
                    .filter { !it.cards.contains(Card.CURSE) }
                    .sortedWith(compareByDescending { it.cards.sumOf { card -> card.vp } } )[0]
                    .takeIf { it.cards.isNotEmpty() } ?: SpecialBranchSelection.SKIP
            }
            else -> randomPolicy.policy(state, branch)
        }
    }
}

