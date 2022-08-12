package policies.delegates.action

import engine.*
import engine.branch.Branch
import engine.card.Card
import engine.branch.BranchSelection
import engine.branch.BranchContext
import engine.card.CardType
import policies.Policy
import policies.PolicyName
import java.lang.IllegalArgumentException

// TODO: make this only handle actions and use a separate policy for treasures wherever this is being used for treasures
class MPPAFPolicy: Policy() {
    override val name = PolicyName("MPPAFPolicy")
    override fun shutdown() = Unit
    override fun policy(
        state: GameState,
        branch: Branch
    ): BranchSelection {
        return if (state.context == BranchContext.CHOOSE_ACTION) {
            val options = branch.getOptions(state).filterIsInstance<Card>()
                .sortedWith(compareBy { it.cost })
                .sortedWith(
                    compareBy(
                        { it.type == CardType.ACTION },
                        { it.addActions > 0 }
                    )).reversed()
            options[0]
        } else {
            throw IllegalArgumentException()
        }
    }
}

