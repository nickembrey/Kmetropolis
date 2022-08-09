package policies

import engine.*
import engine.branch.BranchSelection

@JvmInline
value class PolicyName(val value: String) {
    override fun toString(): String {
        return value
    }
}

// TODO: give policies access to the logger directly instead of having to get it from the GameState
abstract class Policy {
    abstract val name: PolicyName

    open fun endGame() {}
    abstract fun shutdown()
    abstract fun policy (state: GameState): BranchSelection
}