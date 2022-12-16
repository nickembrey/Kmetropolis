package policies.delegates.draw

import engine.GameState
import engine.branch.Branch
import engine.branch.BranchContext
import engine.branch.BranchSelection
import engine.branch.VisibleDrawSelection
import policies.Policy
import policies.PolicyName
import util.input.buildCardSelection

class InputDrawPolicy: Policy() {
    override val name = PolicyName("inputDrawPolicy")
    override fun finally() = Unit


    override fun policy(
        state: GameState,
        branch: Branch
    ): BranchSelection {
        if (branch.context != BranchContext.DRAW) {
            throw IllegalStateException()
        } else {
            val options = listOf(VisibleDrawSelection(cards = buildCardSelection(state, branch)))
            for(option in options.withIndex()) {
                println("(${option.index}): ${option.value}")
            }
            println("")
            print("[${state.currentPlayerNumber}: ${state.currentPlayer.name}] Make your selection: ")
            try {
                val index = readln().toInt()
                if(index !in options.indices) {
                    throw IllegalStateException()
                }
                return options[index]
            } catch(nfe: NumberFormatException) {
                println("")
                print("Invalid input! Please try again.")
                println("")
                return policy(state, branch)
            } catch(ise: IllegalStateException) {
                println("")
                print("Invalid input! Please try again.")
                println("")
                return policy(state, branch)
            }
        }
    }
}