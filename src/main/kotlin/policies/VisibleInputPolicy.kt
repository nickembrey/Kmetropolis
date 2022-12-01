package policies

import engine.*
import engine.branch.*
import policies.delegates.draw.InputDrawPolicy
import policies.delegates.draw.InputVassalDiscardPolicy
import policies.delegates.draw.RandomDrawPolicy

class VisibleInputPolicy : Policy() { // TODO: abstract witch policy

    override val name = PolicyName("visibleInputPolicy")
    override fun finally() = Unit
    override fun policy(
        state: GameState,
        branch: Branch
    ): BranchSelection {

        if(branch.context == BranchContext.DRAW) {
            return InputDrawPolicy()(state, branch)
        } else if(branch.context == BranchContext.VASSAL_DISCARD) {
            return InputVassalDiscardPolicy()(state, branch)
        }

        val options = branch.getOptions(state).toList()
        println(" ___________________________________________________________________________")
        print("|")
        state.board.toMap().toList().forEachIndexed {i, (card, number) ->
            print(" ${card.name}: $number")
            if(number < 10) {
                print(" ")
            }
             for(x in 1..(20-card.name.length)) {
                print(" ")
            }
            if((i + 1) % 3 == 0) {
                print("|\n|")
            }
        }
        for(x in 0..12 * (state.board.size % 3)) {
            print(" ")
        }
        print("|")
        if(state.board.size % 3 != 0) {
            println("")
        }
        println(" ___________________________________________________________________________")
        println("")
        print("Hand: ")
        if(!state.currentPlayer.visibleHand) {
            throw IllegalStateException()
        }
        for(card in state.currentPlayer.knownHand.toList()) {
            print("${card.name} ")
        }
        println("")
        println("")
        print("Buys: ${state.currentPlayer.buys}   ")
        print("Coins: ${state.currentPlayer.coins}   ")
        print("VP: ${state.currentPlayer.vp}")
        println("")
        println("")
        println("Context: ${branch.context}")
        println("")
        println("Options: ")
        println("")
        for(option in options.withIndex()) {
            println("(${option.index}): ${option.value}")
        }
        println("")
        print("Make your selection: ")
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