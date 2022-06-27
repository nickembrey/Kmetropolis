package engine

import policies.Policy

class GameState(
    val playerOne: Player,
    val playerTwo: Player,
    val board: Board = defaultBoard,
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

    var choiceCounter: Int = 0

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
        var choices = context.getCardChoices(choicePlayer, board)
        while (choices.size < 2) {
            if(choices.size == 1) {
                applyDecision(choices, 0)
            } else {
                nextPhase()
            }
            choices = context.getCardChoices(choicePlayer, board)
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
    fun applyDecision(cardChoices: CardChoices, decisionIndex: DecisionIndex) {

        val result = cardChoices[decisionIndex]
        if(result == null) {
            nextPhase()
        } else {
            when (context) {
                ChoiceContext.ACTION -> playActionCard(this, cardChoices, decisionIndex)
                ChoiceContext.TREASURE -> playTreasureCard(this, cardChoices, decisionIndex)
                ChoiceContext.BUY -> buyCard(this, result)
                ChoiceContext.CHAPEL -> {
                    trashCard(choicePlayer, result, verbose)
                    choiceCounter -= 1
                    if(choiceCounter == 0) {
                        nextPhase()
                    }
                }
                ChoiceContext.MILITIA -> {
                    discardCard(choicePlayer, result, verbose)
                    choiceCounter -= 1
                    if(choiceCounter == 0) {
                        nextPhase()
                    }
                }
                ChoiceContext.WORKSHOP -> {
                    gainCard(choicePlayer, result, verbose)
                    nextPhase()
                }
            }
        }
    }

}