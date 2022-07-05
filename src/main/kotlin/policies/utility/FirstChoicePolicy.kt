package policies.utility

import engine.*
import policies.Policy
import policies.PolicyName

object firstChoicePolicy: Policy {
    override val name = PolicyName("firstChoicePolicy")
    override fun policy(
        state: GameState,
        choices: CardChoices
    ): Card? {
        return choices[0]
    }
}

