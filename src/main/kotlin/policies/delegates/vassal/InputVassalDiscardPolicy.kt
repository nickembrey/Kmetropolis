package policies.delegates.draw

import engine.GameState
import engine.branch.*
import policies.Policy
import policies.PolicyName
import util.input.buildCardSelection

class InputVassalDiscardPolicy: Policy() {
    override val name = PolicyName("inputVassalDiscardPolicy")
    override fun finally() = Unit


    override fun policy(
        state: GameState,
        branch: Branch
    ): BranchSelection {
        if (branch.context != BranchContext.VASSAL_DISCARD) {
            throw IllegalStateException()
        } else {
            val options = state.board.possibilities.map { VassalDiscardSelection(card = it) }
            for(option in options.withIndex()) {
                println("(${option.index}): ${option.value}")
            }
            println("")
            print("[${state.currentPlayerNumber}: ${state.currentPlayer.name}] Make your selection: ")
            try {
                val index = readln().toInt()
                if(index !in options.indices) {
                    throw IllegalStateException() // TODO: better exception
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