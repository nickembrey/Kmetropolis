package policies.delegates.action

import engine.*
import engine.branch.*
import engine.card.Card
import engine.card.CardType
import policies.Policy
import policies.PolicyName
import java.lang.IllegalArgumentException

// TODO: make this only handle actions and use a separate policy for treasures wherever this is being used for treasures
class MPPAFPolicy: Policy() {
    override val name = PolicyName("MPPAFPolicy")
    override fun finally() = Unit
    override fun policy(
        state: GameState,
        branch: Branch
    ): BranchSelection {
        return if (branch.context == BranchContext.CHOOSE_ACTION) {
            val options = branch.getOptions(state).filterIsInstance<Card>()
                .sortedWith(compareBy { it.cost })
                .sortedWith(
                    compareBy(
                        { it.type == CardType.ACTION },
                        { it.addActions > 0 }
                    )).reversed()
            if(options.isEmpty()) {
                SpecialBranchSelection.SKIP
            } else {
                ActionSelection(card = options[0])
            }
        } else {
            throw IllegalArgumentException()
        }
    }
}

