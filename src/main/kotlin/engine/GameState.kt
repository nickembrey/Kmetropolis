package engine

import policies.Policy

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
                // turn end
                currentPlayer.discard += currentPlayer.inPlay
                currentPlayer.inPlay = mutableListOf()
                currentPlayer.discard += currentPlayer.hand
                currentPlayer.hand = mutableListOf()
                for (i in 1..5) {
                    drawCard(currentPlayer, !noShuffle)
                }
                currentPlayer.buys = 1
                currentPlayer.coins = 0
                currentPlayer.actions = 1
                turns += 1
                currentPlayer = otherPlayer
                context = ChoiceContext.ACTION
            }
        }
    }

    fun getNextChoices(): CardChoices {
        var choices = getCardChoices(this, choicePlayer, context)
        while (choices.choices.size < 2) {
            if(choices.choices.size == 1) {
                applyDecision(choices, 0)
            } else {
                nextPhase()
            }
            choices = getCardChoices(this, choicePlayer, context)
        }
        return choices
    }

    // TODO: rename
    fun makeNextDecision(policy: Policy) {
        val choices = getNextChoices()
        val decisionIndex = policy(this, choicePlayer, context, choices)
        applyDecision(choices, decisionIndex)
    }

    // TODO: rename
    fun applyDecision(choices: CardChoices, decisionIndex: DecisionIndex) {

        val result = choices.choices[decisionIndex]
        if(result == null) {
            nextPhase()
        } else {
            when (context) {
                ChoiceContext.ACTION -> playActionCard(this, choices as SingleCardChoices, decisionIndex)
                ChoiceContext.TREASURE -> playTreasureCard(this, choices as SingleCardChoices, decisionIndex)
                ChoiceContext.BUY -> buyCard(this, result as Card)
                ChoiceContext.CHAPEL -> {
                    trashCards(choicePlayer, choices as MultipleCardChoices, decisionIndex, verbose)
                    nextPhase()
                }
                ChoiceContext.MILITIA -> {
                    discardCards(choicePlayer, choices as MultipleCardChoices, decisionIndex, verbose)
                    nextPhase()
                }
                ChoiceContext.WORKSHOP -> {
                    decideGainCard(choicePlayer, choices as SingleCardChoices, decisionIndex, verbose)
                    nextPhase()
                }
            }
        }
    }

}