package engine

// TODO: consider renaming to Simulation

class GameState(
    val playerOne: Player,
    val playerTwo: Player,
    val board: Board = defaultBoard,
    var turns: Int = 0,
    var context: ChoiceContext = ChoiceContext.ACTION,
    val trueShuffle: Boolean = true,
    val logger: DominionLogger? = null,
    private val maxTurns: Int = 999
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
        get() = board.filter { it.value == 0}.size >= 3 || board[Card.PROVINCE] == 0 || turns > maxTurns || concede

    val choicePlayer
        get() = if(context == ChoiceContext.MILITIA) {
        otherPlayer
    } else {
        currentPlayer
    }

    // keeps track of how many decisions to spend in a given context
    var contextDecisionCounters: Int = 1
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

    fun nextContext(exitCurrentContext: Boolean = false) { // TODO: debug
        if(contextDecisionCounters < 1 || exitCurrentContext) {
            contextDecisionCounters = 1
            context = when(context) {
                ChoiceContext.ACTION -> ChoiceContext.TREASURE
                ChoiceContext.TREASURE -> ChoiceContext.BUY
                ChoiceContext.REMODEL_TRASH -> when(exitCurrentContext) {
                    true -> ChoiceContext.ACTION
                    false -> ChoiceContext.REMODEL_GAIN
                }
                ChoiceContext.CHAPEL, ChoiceContext.MILITIA, ChoiceContext.WORKSHOP, ChoiceContext.REMODEL_GAIN -> ChoiceContext.ACTION
                ChoiceContext.BUY -> {
                    currentPlayer.endTurn(trueShuffle, logger)
                    turns += 1
                    currentPlayer = otherPlayer
                    ChoiceContext.ACTION
                }
            }
        }
    }

}