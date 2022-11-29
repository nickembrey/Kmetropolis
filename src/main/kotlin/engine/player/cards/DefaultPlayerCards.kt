package engine.player.cards

import engine.card.Card
import engine.performance.util.CardCountMap

// TODO: buys seem to be being added to deck
class DefaultPlayerCards private constructor(
    private val board: CardCountMap,
    override val aside: CardCountMap,
    override val unknownCards: CardCountMap,
    override val knownHand: CardCountMap,
    override val inPlay: CardCountMap,
    override val knownDeck: MutableMap<Int, Card>,
    override val knownDiscard: CardCountMap,
    override val trash: CardCountMap,
    override var vassalCard: Card?,
    override var handCount: Int,
    override var deckCount: Int,
    override var discardCount: Int
): PlayerCards {

    companion object {
        fun new(
            board: CardCountMap,
            initialDeck: CardCountMap
        ) = DefaultPlayerCards(
            board = board,
            aside = CardCountMap.empty(board),
            unknownCards = initialDeck,
            knownHand = CardCountMap.empty(board),
            inPlay = CardCountMap.empty(board),
            knownDeck = mutableMapOf(),
            knownDiscard = CardCountMap.empty(board),
            trash = CardCountMap.empty(board),
            vassalCard = null,
            handCount = 0,
            deckCount = initialDeck.size,
            discardCount = 0
        )
    }

    override fun copy(): DefaultPlayerCards =
        DefaultPlayerCards(
            board = board.copy(),
            aside = aside.copy(),
            unknownCards = unknownCards.copy(),
            knownHand = knownHand.copy(),
            inPlay = inPlay.copy(),
            knownDeck = knownDeck.toMutableMap(),
            knownDiscard = knownDiscard.copy(),
            trash = trash.copy(),
            vassalCard = vassalCard,
            handCount = handCount,
            deckCount = deckCount,
            discardCount = discardCount
        )

    override val allCards: CardCountMap
        get() = unknownCards + knownHand + inPlay + CardCountMap.fromKnownDeck(board, knownDeck) + knownDiscard + trash

    override fun sample(n: Int): List<Card> {

        val unknownCardsCopy = unknownCards.copy()
        val knownDiscardCopy = knownDiscard.copy()

        return (0 until n).map { index ->
            if(knownDeck[index] != null) {
                knownDeck[index]!!
            } else if(deckCount > n || index < deckCount) {
                unknownCardsCopy.random().also { card ->
                    unknownCardsCopy[card] -= 1
                }
            } else {
                (unknownCardsCopy + knownDiscardCopy).random().also { card ->
                    if(knownDiscardCopy[card] > 0) {
                        knownDiscardCopy[card] -= 1
                    } else {
                        unknownCardsCopy[card] -= 1
                    }
                }
            }
        }
    }

    override fun cleanup() {
        knownDiscard.add(inPlay)
        discardCount += inPlay.size
        inPlay.clear()
    }

    override fun shuffle() {
        unknownCards.add(knownDiscard)
        deckCount += discardCount
        discardCount = 0
        knownDiscard.clear()
    }

    override fun redeck() {
        unknownCards.add(knownHand)
        deckCount += handCount
        handCount = 0
        knownHand.clear()
    }

    override fun draw(card: Card) {
        if(knownDeck[0] != null) {
            knownDeck.remove(0)
            for(entry in knownDeck.entries) { // TODO: sort
                knownDeck.remove(entry.key)
                knownDeck[entry.key - 1] = entry.value
            }
        } else if(deckCount == 0) {
            shuffle()
            unknownCards[card] -= 1
        } else {
            unknownCards[card] -= 1
        }
        deckCount -= 1
        handCount += 1
        knownHand[card] += 1
    }

    override fun play(card: Card) {
        if(knownHand[card] > 0) {
            knownHand[card] -= 1
        } else {
            unknownCards[card] -= 1
        }
        handCount -= 1
        inPlay[card] += 1
    }

    override fun playFromDiscard(card: Card) {
        if(knownDiscard[card] > 0) {
            knownDiscard[card] -= 1
        } else {
            unknownCards[card] -= 1
        }
        discardCount -= 1
        inPlay[card] -= 1
    }

    override fun gain(card: Card) {
        knownDiscard[card] += 1
        discardCount += 1
    }

    override fun gainToHand(card: Card) {
        knownHand[card] += 1
        handCount += 1
    }

    override fun topdeck(card: Card) { // TODO: does harbinger reveal the card? I think so
        if(knownDiscard[card] > 0) {
            knownDiscard[card] -= 1
        } else {
            unknownCards[card] -= 1
        }
        discardCount -= 1
        for(entry in knownDeck.entries) {
            knownDeck.remove(entry.key)
            knownDeck[entry.key + 1] = entry.value
        }
        knownDeck[0] = card
        deckCount += 1
    }

    override fun discard(card: Card) {
        knownDiscard[card] += 1
        if(knownHand[card] > 0) {
            knownHand[card] -= 1
        } else {
            unknownCards[card] -= 1
        }
        handCount -= 1
        discardCount += 1
    }

    override fun discardFromAside(card: Card) {
        knownDiscard[card] += 1
        discardCount += 1
        aside[card] -= 1
    }

    override fun trash(card: Card) {
        trash[card] += 1
        if(knownHand[card] > 0) {
            knownHand[card] -= 1
        } else {
            unknownCards[card] -= 1
        }
        handCount -= 1
    }

    override val visibleHand: Boolean
        get() = knownHand.size == handCount

    override fun setAside(index: Int) {
        val card = knownDeck[index]!!
        deckCount -= 1
        aside[card] += 1
        knownDeck.remove(index)
        for(i in knownDeck.keys.sorted()) {
            if(i > index) {
                knownDeck[i-1] = knownDeck[i]!!
                knownDeck.remove(i)
            }
        }
    }

    override fun identify(card: Card, index: Int) {
        unknownCards[card] -= 1
        knownDeck[index] = card
    }
}