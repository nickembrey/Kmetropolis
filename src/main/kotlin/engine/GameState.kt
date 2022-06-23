import engine.*
import engine.player.Player

class GameState(
    val playerOne: Player,
    val playerTwo: Player,
    val board: MutableMap<Card, Int> = mutableMapOf(
        Card.FESTIVAL to 10,
        Card.WITCH to 10,
        Card.MARKET to 10,
        Card.LABORATORY to 10,
        Card.SMITHY to 10,
        Card.MONEYLENDER to 10,
        Card.MILITIA to 10,
        Card.CHAPEL to 10,
        Card.VILLAGE to 10,
        Card.WORKSHOP to 10,

        Card.GOLD to 30,
        Card.SILVER to 40,
        Card.COPPER to 46,

        Card.PROVINCE to 8,
        Card.DUCHY to 8,
        Card.ESTATE to 8,

        Card.CURSE to 10,
    ),
    var turns: Int = 0,
    var context: ChoiceContext = ChoiceContext.ACTION,
    val noShuffle: Boolean = false,
    val verbose: Boolean = false,
    val logger: DominionLogger = DominionLogger()
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

    fun initialize() {
        playerOne.deck.shuffle()
        playerTwo.deck.shuffle()
        for(i in 1..5) {
            drawCard(playerOne, !noShuffle)
            drawCard(playerTwo, !noShuffle)
        }
    }

    fun next() {
        val decision = choicePlayer.getDecision(this)
        choicePlayer.makeDecision(this, decision)
    }

}