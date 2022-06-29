package engine

data class Player(
    val name: String, // TODO: names should always be just the policy name + numeric tag or hash
    val playerNumber: PlayerNumber,
    val defaultPolicy: (GameState, Player, ChoiceContext, CardChoices) -> DecisionIndex,
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

    var actions = 1
    var buys = 1
    var coins = 0

    val allCards
        get() = deck + hand + discard + inPlay

    val vp
        get() = allCards.sumOf { it.vp }

    fun playCard(card: Card, state: GameState, logger: DominionLogger? = null) {

        logger?.log("$name plays ${card.name}")

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

        drawCards(card.addCards, state.trueShuffle)
    }

    fun buyCard(card: Card, board: Board, logger: DominionLogger? = null) {
        logger?.log("$name buys ${card.name}")
        coins -= card.cost
        buys -= 1
        gainCard(card, board, logger)
    }

    // TODO: use for witch
    fun gainCard(card: Card, board: Board, logger: DominionLogger? = null) {
        logger?.log("$name gains ${card.name}")
        board[card] = board[card]!! - 1
        discard.add(card)
    }

    fun trashCard(card: Card, logger: DominionLogger? = null) {
        logger?.log("$name trashes ${card.name}")
        hand.remove(card)
    }

    fun discardCard(card: Card, logger: DominionLogger? = null) {
        logger?.log("$name discards ${card.name}")
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

    // TODO: validate that card is in hand / play?
    fun makeCardDecision(card: Card, state: GameState, logger: DominionLogger? = null) {
        when (state.context) {
            ChoiceContext.ACTION, ChoiceContext.TREASURE -> playCard(card, state, logger)
            ChoiceContext.BUY -> buyCard(card, state.board, logger)
            ChoiceContext.CHAPEL -> {
                trashCard(card, logger)
                state.choiceCounter -= 1 // TODO: try to move this up
                if(state.choiceCounter == 0) {
                    state.nextPhase()
                }
            }
            ChoiceContext.MILITIA -> {
                discardCard(card, logger)
                state.choiceCounter -= 1  // TODO: try to move this up
                if(state.choiceCounter == 0) {
                    state.nextPhase()
                }
            }
            ChoiceContext.WORKSHOP -> {
                gainCard(card, state.board, logger)
                state.nextPhase()
            }
        }
    }

    fun endTurn(trueShuffle: Boolean) {
        discard += inPlay
        inPlay.clear()
        discard += hand
        hand.clear()
        drawCards(5, trueShuffle)
        actions = 1
        buys = 1
        coins = 0

    }

}