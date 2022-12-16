package policies

import engine.*
import engine.branch.*


class RemoteInputPolicy : Policy(hidden = true) {

    override val name = PolicyName("remoteInputPolicy")
    override fun finally() = Unit
    override fun policy(
        state: GameState,
        branch: Branch
    ): BranchSelection {

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
            println("")
            print("Invalid input! Please try again.")
            println("")
            return policy(state, branch)
        }

        return options[index]
    }
}