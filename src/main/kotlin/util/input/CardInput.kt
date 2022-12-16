package util.input

import engine.GameState
import engine.branch.Branch
import engine.card.Card

fun buildCardSelection(
    state: GameState,
    branch: Branch,
    cardList: List<Card> = listOf()
): List<Card> {
    val options = state.board.possibilities
    println("Player: ${state.currentPlayer.playerNumber} (${state.currentPlayer.name})")
    println("Context: ${branch.context}")
    println("Selections: ${branch.options}")
    println("Current selection: $cardList")
    println("")
    println("Options: ")
    println("")
    for( (index, card) in options.withIndex()) {
        print("(${index}): ${card}")
        if((index + 1) % 5 == 0) {
            println("")
        } else {
            for(letter in 1..(20 - card.name.length)) {
                print(" ")
            }
            if(index < 10) {
                print(" ")
            }
        }
    }
    println("(X): END SELECTION")
    println("")
    print("Make your selection: ")
    val selection = readln()
    println("")
    return if(selection != "X") {
        try {
            buildCardSelection(state, branch, cardList.plus(options[selection.toInt()]))
        } catch(nfe: NumberFormatException) {
            println("")
            println("Invalid input! Please start again.")
            println("")
            return buildCardSelection(state, branch)
        }

    } else {
        cardList
    }
}