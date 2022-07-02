package policies.utility

import engine.*
import policies.Policy

object firstChoicePolicy: Policy {
    override val name = "firstChoicePolicy"
    override fun policy(
        state: GameState,
        choices: CardChoices
    ): Card? {
        return choices[0]
    }
}

