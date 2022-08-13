package policies

import engine.*
import engine.branch.Branch
import engine.branch.BranchSelection

@JvmInline
value class PolicyName(val value: String) {
    override fun toString(): String {
        return value
    }
}

// TODO: give policies access to the logger directly instead of having to get it from the GameState

// TODO: put policy as argument and have it implement (GameState, Branch) -> BranchSelection by policy
//       -- or better still, just add an invoke and have it call policy and make policy private?
abstract class Policy(
// TODO: allow timed rollouts
) {
    abstract val name: PolicyName

    open fun endGame() {}
    abstract fun shutdown()
    abstract fun policy(
        state: GameState,
        branch: Branch
    ): BranchSelection
}