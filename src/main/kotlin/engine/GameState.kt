package engine

class GameState(
    val playerOne: Player,
    val playerTwo: Player,
    val board: Board = defaultBoard,
    var turns: Int = 0,
    var context: ChoiceContext = ChoiceContext.ACTION,
    val trueShuffle: Boolean = true,

    val logger: DominionLogger? = null
) {

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

    // keeps track of how many choices to spend in a given context
    var choiceContextCounter: Int = 1
        get() = when(context) {
            ChoiceContext.ACTION -> currentPlayer.actions
            ChoiceContext.TREASURE -> currentPlayer.hand.filter { it.type == CardType.TREASURE }.size
            ChoiceContext.BUY -> currentPlayer.buys
            ChoiceContext.MILITIA -> choicePlayer.hand.size - 3
            else -> field
        }

    fun initialize() {
        playerOne.deck.shuffle()
        playerTwo.deck.shuffle()
        playerOne.drawCards(5, trueShuffle)
        playerTwo.drawCards(5, trueShuffle)
    }

    fun nextContext(exitCurrentContext: Boolean = false) {
        if(choiceContextCounter > 0 && !exitCurrentContext) {
            choiceContextCounter -= 1
        } else {
            choiceContextCounter = 1
            context = when(context) {
                ChoiceContext.ACTION -> ChoiceContext.TREASURE
                ChoiceContext.TREASURE -> ChoiceContext.BUY
                ChoiceContext.CHAPEL, ChoiceContext.MILITIA, ChoiceContext.WORKSHOP -> ChoiceContext.ACTION
                ChoiceContext.BUY -> {
                    currentPlayer.endTurn(trueShuffle)
                    turns += 1
                    currentPlayer = otherPlayer
                    ChoiceContext.ACTION
                }
            }
        }
    }

}