package policies.rollout

import engine.*
import policies.Policy
import policies.PolicyName

class RandomPolicy : Policy() {
    override val name = PolicyName("randomPolicy")
    override fun policy(
        state: GameState,
        choices: CardChoices
    ): Card? {
        return choices.random()
    }
}

