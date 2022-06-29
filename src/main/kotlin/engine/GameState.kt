package engine

import engine.simulation.getNextCardChoices
import policies.Policy

class GameState(
    val playerOne: Player,
    val playerTwo: Player,
    val board: Board = defaultBoard,
    var turns: Int = 0,
    var context: ChoiceContext = ChoiceContext.ACTION,
    val noShuffle: Boolean = false, // TODO: confusing name

    val logger: DominionLogger? = null
) {

    val verbose: Boolean
        get() = logger != null

    var currentPlayer: Player = playerOne
    val otherPlayer: Player
        get() = if(currentPlayer == playerOne) {
            playerTwo
        } else {
            playerOne
        }

    var concede = false

    val gameOver
        get() = board.filter { it.value == 0}.size >= 3 || board[Card.PROVINCE] == 0 || turns > 100 || concede

    val choicePlayer
        get() = if(context == ChoiceContext.MILITIA) {
        otherPlayer
    } else {
        currentPlayer
    }

    var choiceCounter: Int = 0

    fun initialize() {
        playerOne.deck.shuffle()
        playerTwo.deck.shuffle()
        playerOne.drawCards(5, !noShuffle)
        playerTwo.drawCards(5, !noShuffle)
    }

    fun nextPhase() {
        when(context) {
            ChoiceContext.ACTION -> {
                context = ChoiceContext.TREASURE
            }
            ChoiceContext.TREASURE -> {
                context = ChoiceContext.BUY
            }
            ChoiceContext.CHAPEL, ChoiceContext.MILITIA, ChoiceContext.WORKSHOP -> {
                context = ChoiceContext.ACTION
            }
            ChoiceContext.BUY -> {
                currentPlayer.endTurn(noShuffle)
                turns += 1
                currentPlayer = otherPlayer
                context = ChoiceContext.ACTION
            }
        }
    }

}