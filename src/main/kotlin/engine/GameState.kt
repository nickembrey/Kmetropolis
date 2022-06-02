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
//        Card.MILITIA to 10,
//        Card.CHAPEL to 10,
        Card.VILLAGE to 10,
//        Card.WORKSHOP to 10,

        Card.GOLD to 30,
        Card.SILVER to 40,
        Card.COPPER to 46,

        Card.PROVINCE to 8,
        Card.DUCHY to 8,
        Card.ESTATE to 8,

        Card.CURSE to 10,
    ),
    var turns: Int = 0,
    var status: Pair<Player, TurnPhase> = Pair(playerOne, TurnPhase.ACTION),
    val noShuffle: Boolean = false,
    val logger: DominionLogger = DominionLogger()
) {


    var concede = false

    val gameOver
        get() = board.filter { it.value == 0}.size >= 3 || board[Card.PROVINCE] == 0 || turns > 100 || concede

    val currentPlayer
        get() = status.first
    val otherPlayer
        get() = if (currentPlayer == playerOne) {
        playerTwo
    } else {
        playerOne
    }

    val currentPhase: TurnPhase
        get() = status.second

    fun initialize() {
        playerOne.deck.shuffle()
        playerTwo.deck.shuffle()
        for(i in 1..5) {
            drawCard(playerOne, !noShuffle)
            drawCard(playerTwo, !noShuffle)
        }
    }

    fun next() {
        when(currentPhase) {
            TurnPhase.ACTION -> {
                turns += 1
                currentPlayer.buys = 1
                currentPlayer.coins = 0
                currentPlayer.actions = 1
                val decision = currentPlayer.getDecision(this, ChoiceContext.ACTION)
                currentPlayer.makeDecision(this, ChoiceContext.ACTION, decision)
            }
            TurnPhase.BUY -> {
                currentPlayer.coins += currentPlayer.hand.filter { it.type == CardType.TREASURE}.sumOf { it.addCoins }
                val decision = currentPlayer.getDecision(this, ChoiceContext.BUY)
                currentPlayer.makeDecision(this, ChoiceContext.BUY, decision)
            }
        }
    }

    fun nextContext(): ChoiceContext {
        when(currentPhase) {
            TurnPhase.ACTION -> {
                turns += 1
                currentPlayer.buys = 1
                currentPlayer.coins = 0
                currentPlayer.actions = 1
                return if (currentPlayer.hand.none { it.type == CardType.ACTION }) {
                    // skip if there are no actions to do
                    currentPlayer.coins += currentPlayer.hand.filter { it.type == CardType.TREASURE}.sumOf { it.addCoins }
                    ChoiceContext.BUY
                } else {
                    ChoiceContext.ACTION
                }

            }
            TurnPhase.BUY -> {
                currentPlayer.coins += currentPlayer.hand.filter { it.type == CardType.TREASURE}.sumOf { it.addCoins }
                return ChoiceContext.BUY
            }
        }
    }

}