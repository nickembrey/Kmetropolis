package policies

import engine.*
import engine.branch.*
import policies.delegates.draw.RandomDrawPolicy

class InputPolicy : Policy() { // TODO: abstract witch policy

    private val randomDrawPolicy = RandomDrawPolicy()

    override val name = PolicyName("inputPolicy")
    override fun finally() = Unit
    override fun policy(
        state: GameState,
        branch: Branch
    ): BranchSelection {

        if(branch.context == BranchContext.DRAW) {
            return randomDrawPolicy(state, branch)
        }

        val options = branch.getOptions(state).toList()
        println(" ___________________________________________________________________________")
        print("|")
        state.board.entries.forEachIndexed { i, (card, number) ->
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
        for(card in state.currentPlayer.hand) {
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
        val index = readln().toInt()
        if(index !in options.indices) {
            throw IllegalStateException() // TODO: better exception
        }

        return options[index]
    }
}