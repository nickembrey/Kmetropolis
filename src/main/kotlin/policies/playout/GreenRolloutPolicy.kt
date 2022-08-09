package policies.playout

import engine.*
import engine.card.Card
import engine.branch.BranchContext
import engine.branch.BranchSelection
import engine.branch.SpecialBranchSelection
import engine.card.CardType
import policies.Policy
import policies.PolicyName
import policies.delegates.draw.RandomDrawPolicy
import policies.utility.RandomPolicy

class GreenRolloutPolicy : Policy() {

    companion object {
        val randomPolicy = RandomPolicy()
        val randomDrawPolicy = RandomDrawPolicy()
    }

    private val cardMenu: ArrayList<Card> = ArrayList(20)
    override val name = PolicyName("greenRolloutPolicy")
    override fun shutdown() = Unit
    override fun policy(state: GameState): BranchSelection {

        val options = state.context.toOptions(state)

        return when(state.context) {
            BranchContext.DRAW -> randomDrawPolicy.policy(state)
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
            BranchContext.CHOOSE_BUY -> {
                cardMenu.clear()
                options
                    .filterIsInstanceTo(cardMenu)
                    .apply { remove(Card.CURSE) }
                    .sortWith(compareByDescending<Card> { it.type == CardType.VICTORY }.thenByDescending { it.cost } )
                return if(cardMenu.isNotEmpty()) {
                    cardMenu[0]
                } else {
                    SpecialBranchSelection.SKIP
                }
            }
            else -> randomPolicy.policy(state)
        }
    }
}

