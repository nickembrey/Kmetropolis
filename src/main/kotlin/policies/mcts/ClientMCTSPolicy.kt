package policies.mcts

import engine.GameState
import engine.branch.Branch
import engine.branch.BranchContext
import engine.branch.BranchSelection
import policies.VisibleInputPolicy
import policies.Policy
import policies.PolicyName
import policies.delegates.draw.InputDrawPolicy
import policies.mcts.node.NodeValueFn
import policies.mcts.rollout.score.RolloutScoreFn

class ClientMCTSPolicy(
    cParameter: Double,
    rollouts: Int,
    rolloutPolicy: Policy,
    rolloutScoreFn: RolloutScoreFn,
    nodeValueFn: NodeValueFn,
): Policy() {

    override val name = PolicyName(
        "MCTSClientPolicy ($cParameter, $rollouts, ${rolloutPolicy.name}, ${rolloutScoreFn.name})"
    )

    val inputPolicy = VisibleInputPolicy()

    val basePolicy = MCTSPolicy(
        cParameter,
        rollouts,
        rolloutPolicy,
        rolloutScoreFn,
        nodeValueFn,
        drawPolicy = InputDrawPolicy()
    )

    override fun policy(state: GameState, branch: Branch): BranchSelection {
        val selection = basePolicy(state, branch)
        if(branch.context == BranchContext.DRAW) {
            return selection
        } else {
            println("Player: ${state.currentPlayer.playerNumber} (${state.currentPlayer.name})")
            println("Context: ${branch.context}")
            println("Selections: ${branch.selections}")
            println("")
            println("Recommended selection: $selection")
            println("")
            return inputPolicy(state, branch)
        }
    }

    override fun finally() = Unit


}