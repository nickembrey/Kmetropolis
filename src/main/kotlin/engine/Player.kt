package engine

import policies.policy.Policy
import java.lang.Math.floor

data class Player(
    val name: String, // TODO: names should always be just the policy name + numeric tag or hash
    val playerNumber: PlayerNumber,
    val defaultPolicy: Policy,
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

    var remodelCard: Card? = null

    val allCards
        get() = deck + hand + discard + inPlay

    val vp
        get() = allCards.sumOf { it.vp } + (allCards.count { it == Card.GARDENS } * kotlin.math.floor(allCards.size.toDouble() / 10)).toInt()

    // TODO: one way we could make this more functional is by adding the notion of an Effect type,
    //       which playing a card would return and could be passed up to the state to be processed
    fun playCard(card: Card, state: GameState, logger: DominionLogger? = null) {

        logger?.log("$name plays ${card.name}")

        hand.remove(card).also { removed ->
            if (!removed) {
                throw IllegalStateException("Can't play a card that wasn't in hand!")
            }
        }

        inPlay.add(card)

        if (card.type == CardType.ACTION) {
            actions -= 1
        }

        buys += card.addBuys
        actions += card.addActions
        coins += card.addCoins

        for (effect in card.effectList) {
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
        hand.remove(card).also { removed ->
            if (!removed) {
                throw IllegalStateException("Can't trash a card that wasn't in hand!")
            }
        }

        logger?.log("$name trashes ${card.name}")

    }

    // TODO: one moveCard method that handles all this, with different destinations as enum
    fun discardCard(card: Card, logger: DominionLogger? = null) {
        hand.remove(card).also { removed ->
            if (!removed) {
                throw IllegalStateException("Can't discard a card that wasn't in hand!")
            }
        }

        discard.add(card)

        logger?.log("$name discards ${card.name}")
    }

    fun drawCard(trueShuffle: Boolean = true) {
        if (deck.size == 0) {
            shuffle(trueShuffle)
        }
        if (deck.size > 0) {
            hand.add(deck[0])
            deck.removeAt(0) //  TODO: may need to test this
        }
    }

    fun drawCards(number: Int, trueShuffle: Boolean = true) {
        if (number > 0) {
            drawCard(trueShuffle)
            drawCards(number - 1, trueShuffle)
        }
    }

    fun shuffle(trueShuffle: Boolean = true) {
        deck += discard
        discard.clear()
        if(trueShuffle) {
            deck.shuffle()
        }
    }

    fun makeCardDecision(card: Card?, state: GameState, logger: DominionLogger? = null) {

        val context = state.context

        card?.let { it ->
            when (context) {
                ChoiceContext.ACTION, ChoiceContext.TREASURE -> playCard(it, state, logger)
                ChoiceContext.BUY -> buyCard(it, state.board, logger)
                ChoiceContext.CHAPEL -> trashCard(it, logger)
                ChoiceContext.MILITIA -> discardCard(it, logger)
                ChoiceContext.WORKSHOP -> gainCard(it, state.board, logger)
                ChoiceContext.REMODEL_TRASH -> {
                    trashCard(it, logger).also { _ -> remodelCard = it }
                }
                ChoiceContext.REMODEL_GAIN -> gainCard(it, state.board, logger).also { _ -> remodelCard = null }
            }
        }

        // TODO: the need for management here probably means the contextDecisionCounter needs to be rethought and nextContext should be scrapped or redesigned
        if(state.context == context) { // decrement the decision counters unless we changed context
            state.contextDecisionCounters -= 1
            state.nextContext(card == null)
        }
    }

    fun makeNextCardDecision(state: GameState, policy: Policy = defaultPolicy) {
        state.context.getCardChoices(this, state.board).let {
            when(it.size) {
                1 -> makeCardDecision(it[0], state, state.logger)
                else -> makeCardDecision(policy(state, it), state, state.logger)
            }
        }
    }


    fun endTurn(trueShuffle: Boolean, logger: DominionLogger? = null) {

        discard += inPlay
        inPlay.clear()
        discard += hand
        hand.clear()
        drawCards(5, trueShuffle)
        actions = 1
        buys = 1
        coins = 0
        logger?.log("\n${this.name} ends their turn\n")
    }

}