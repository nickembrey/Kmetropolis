package policies.delegates.action

import engine.*
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
    override fun policy(state: GameState): BranchSelection {
        return if (state.context == BranchContext.CHOOSE_ACTION) {
            state.context.toOptions(state).sortedWith(
                compareBy(
                    {it is Card && it.type == CardType.ACTION},
                    {it is Card && it.addActions > 0 }
                ) ).reversed()[0]
        } else {
            throw IllegalArgumentException()
        }
    }
}

