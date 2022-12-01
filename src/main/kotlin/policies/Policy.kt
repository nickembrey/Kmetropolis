package policies

import engine.*
import engine.branch.Branch
import engine.branch.BranchSelection

// TODO: allow timed rollouts

// TODO: give policies access to the logger directly instead of having to get it from the GameState

abstract class Policy(val hidden: Boolean = false) {
    abstract val name: PolicyName
    protected abstract fun policy(state: GameState, branch: Branch): BranchSelection
    protected abstract fun finally()
    operator fun invoke(state: GameState, branch: Branch): BranchSelection = policy(state, branch).also { finally() }
}