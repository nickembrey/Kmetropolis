package policies.rollout

import engine.*
import policies.Policy

object randomPolicy : Policy {
    override val name = "randomPolicy"
    override fun policy(
        state: GameState,
        choices: CardChoices
    ): Card? {
        return choices.random()
    }
}

