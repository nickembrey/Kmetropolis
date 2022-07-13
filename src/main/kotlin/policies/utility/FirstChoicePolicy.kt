package policies.utility

import engine.*
import engine.card.Card
import policies.Policy
import policies.PolicyName

class FirstChoicePolicy: Policy() {
    override val name = PolicyName("firstChoicePolicy")
    override fun policy(
        state: GameState,
        choices: CardChoices
    ): Card? {
        return choices[0]
    }
}

