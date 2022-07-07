package policies

import engine.*

@JvmInline
value class PolicyName(val value: String) {
    override fun toString(): String {
        return value
    }
}

abstract class Policy {
    abstract val name: PolicyName
    abstract fun policy (state: GameState, choices: CardChoices): Card?
}