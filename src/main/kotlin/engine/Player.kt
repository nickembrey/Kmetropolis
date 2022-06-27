package engine

data class Player(
    val name: String,
    val playerNumber: PlayerNumber,
    val policy: (GameState, Player, ChoiceContext, CardChoices) -> DecisionIndex,
    var deck: MutableList<Card> = mutableListOf(
        Card.COPPER,
        Card.COPPER,
        Card.COPPER,
        Card.COPPER,
        Card.COPPER,
        Card.COPPER,
        Card.COPPER,
        Card.ESTATE,
        Card.ESTATE,
        Card.ESTATE
    ),
    var hand: MutableList<Card> = mutableListOf(),
    var inPlay: MutableList<Card> = mutableListOf(),
    var discard: MutableList<Card> = mutableListOf()
    ) {

    var actions = 0
    var buys = 1
    var coins = 0

    val allCards
        get() = deck + hand + discard + inPlay

    val vp
        get() = allCards.sumOf { it.vp }

    fun playCard(card: Card, state: GameState, verbose: Boolean = false) {

        if(verbose) {
            println("$name plays ${card.name}")
        }

        hand.remove(card)
        inPlay.add(card)
        if(card.type == CardType.ACTION) {
            actions -= 1
        }

        buys += card.addBuys
        actions += card.addActions
        coins += card.addCoins

        for(effect in card.effectList) {
            effect(state)
        }

        drawCards(card.addCards, !state.noShuffle)
    }

    fun buyCard(card: Card, board: Board, verbose: Boolean = false) {
        if(verbose) {
            println("$name buys ${card.name}")
        }
        coins -= card.cost
        buys -= 1
        board[card] = board[card]!! - 1
        gainCard(card, verbose)
    }

    fun gainCard(card: Card, verbose: Boolean = false) {
        if(verbose) {
            println("$name gains ${card.name}")
        }
        discard.add(card)
    }

    fun trashCard(card: Card, verbose: Boolean = false) {
        if(verbose) {
            println("$name trashes ${card.name}")
        }
        hand.remove(card)
    }

    fun discardCard(card: Card, verbose: Boolean = false) {
        if(verbose) {
            println("$name discards ${card.name}")
        }
        hand.remove(card)
        discard.add(card)
    }

    fun drawCards(number: Int, trueShuffle: Boolean = true) {
        if(number > 0) {
            drawCard(trueShuffle)
            drawCards(number - 1, trueShuffle)
        }
    }

    fun drawCard(trueShuffle: Boolean = true) {
        if(deck.size == 0) {
            shuffle(trueShuffle)
        }
        if(deck.size > 0) {
            hand.add(deck[0])
            deck.removeAt(0) //  TODO: may need to test this
        }
    }

    fun shuffle(trueShuffle: Boolean = true) {
        deck += discard
        discard.clear()
        if(trueShuffle) {
            deck.shuffle()
        }
    }

    fun makeCardDecision(card: Card, state: GameState, verbose: Boolean = false) {
        when (state.context) {
            ChoiceContext.ACTION, ChoiceContext.TREASURE -> playCard(card, state, verbose)
            ChoiceContext.BUY -> buyCard(card, state.board, verbose)
            ChoiceContext.CHAPEL -> {
                trashCard(card, verbose)
                state.choiceCounter -= 1 // TODO: try to move this up
                if(state.choiceCounter == 0) {
                    state.nextPhase()
                }
            }
            ChoiceContext.MILITIA -> {
                discardCard(card, verbose)
                state.choiceCounter -= 1  // TODO: try to move this up
                if(state.choiceCounter == 0) {
                    state.nextPhase()
                }
            }
            ChoiceContext.WORKSHOP -> {
                gainCard(card, verbose)
                state.nextPhase()
            }
        }
    }

    fun endTurn(noShuffle: Boolean) {
        discard += inPlay
        inPlay.clear()
        discard += hand
        hand.clear()
        drawCards(5, !noShuffle)
        buys = 1
        coins = 0
        actions = 1
    }

}