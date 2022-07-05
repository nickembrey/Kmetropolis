package policies

import engine.*

@JvmInline
value class PolicyName(val value: String) {
    override fun toString(): String {
        return value
    }
}

interface Policy {
    val name: PolicyName
    fun policy (state: GameState, choices: CardChoices): Card?
}