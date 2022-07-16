package policies.utility

import engine.*
import engine.card.Card
import policies.Policy
import policies.PolicyName

class RandomPolicy : Policy() {
    override val name = PolicyName("randomPolicy")
    override fun shutdown() = Unit
    override fun policy(
        state: GameState,
        choices: CardChoices
    ): Card? {
        return choices.random()
    }
}