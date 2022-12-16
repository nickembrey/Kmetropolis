package engine.player.cards

import engine.card.Card
import engine.performance.util.CardCountMap

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
        get() = unknownCards + knownHand + inPlay + CardCountMap.fromKnownDeck(board, knownDeck) + knownDiscard

    override fun sample(n: Int): List<Card> {

        val maxN = if(n > (deckCount + discardCount)) {
            deckCount + discardCount
        } else {
            n
        }

        return(_sample(maxN))
    }

    private fun _sample(
        n: Int
    ): List<Card> {

        val cardList: MutableList<Card> = mutableListOf()
        val unknownCardList: MutableList<Card> = mutableListOf()
        val knownDiscardCardList: MutableList<Card> = mutableListOf()

        var combined: CardCountMap?

        for(index in 0 until n) {
            if(knownDeck[index] != null) {
                cardList.add(knownDeck[index]!!)
            } else if(index < deckCount) {
                val card = unknownCards.random()
                unknownCards[card] -= 1
                unknownCardList.add(card)
            } else {
                combined = (unknownCards + knownDiscard)
                val card = combined.random()
                combined[card] -= 1
                if(knownDiscard[card] > 0) {
                    knownDiscard[card] -= 1
                    knownDiscardCardList.add(card)
                } else {
                    unknownCards[card] -= 1
                    unknownCardList.add(card)
                }
            }
        }
        for(card in unknownCardList) {
            cardList.add(card)
            unknownCards[card] += 1
        }

        for(card in knownDiscardCardList) {
            cardList.add(card)
            knownDiscard[card] += 1
        }

        return cardList
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

    override fun hiddenDraw() {
        if(deckCount == 0) {
            shuffle()
        }
        if(deckCount != 0) {
            deckCount -= 1
            handCount += 1
        }
    }

    override fun visibleDraw(card: Card) {
        if(knownDeck[0] != null) {
            knownDeck.remove(0)
            for(entry in knownDeck.entries.toList().sortedBy { it.key }) {
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
        inPlay[card] += 1
    }

    override fun gain(card: Card) {
        knownDiscard[card] += 1
        discardCount += 1
    }

    override fun gainToHand(card: Card) {
        knownHand[card] += 1
        handCount += 1
    }

    override fun topdeck(card: Card) {
        if(knownDiscard[card] > 0) {
            knownDiscard[card] -= 1
        } else {
            unknownCards[card] -= 1
        }
        discardCount -= 1
        for(entry in knownDeck.entries.toList().sortedByDescending { it.key }) {
            knownDeck.remove(entry.key)
            knownDeck[entry.key + 1] = entry.value
        }
        knownDeck[0] = card
        deckCount += 1
    }

    override fun topdeckFromHand(card: Card) {
        if(knownHand[card] > 0) {
            knownHand[card] -= 1
        } else {
            unknownCards[card] -= 1
        }
        handCount -= 1
        for(entry in knownDeck.entries.toList().sortedByDescending { it.key }) {
            knownDeck.remove(entry.key)
            knownDeck[entry.key + 1] = entry.value
        }
        knownDeck[0] = card
        deckCount += 1
    }

    override fun hiddenDiscard() {
        handCount -= 1
        discardCount += 1
    }

    override fun visibleDiscard(card: Card) {
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

    override fun hiddenDiscardFromDeck(index: Int) {
        deckCount -= 1
        discardCount += 1
    }

    override fun visibleDiscardFromDeck(index: Int): Card {
        val card = knownDeck[index]!!

        knownDeck.remove(index)
        for(entry in knownDeck.entries.toList().sortedBy { it.key }) {
            if(entry.key > index) {
                knownDeck.remove(entry.key)
                knownDeck[entry.key - 1] = entry.value
            }
        }

        knownDiscard[card] += 1
        deckCount -= 1
        discardCount += 1
        return card
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

    override fun trashFromDeck(index: Int) {
        val card = knownDeck[index]!!

        knownDeck.remove(index)
        for(entry in knownDeck.entries.toList().sortedBy { it.key }) {
            if(entry.key > index) {
                knownDeck.remove(entry.key)
                knownDeck[entry.key - 1] = entry.value
            }
        }

        trash[card] += 1
        deckCount -= 1
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
        if(deckCount == 0) {
            shuffle()
        }
        if(knownDeck[index] == null) {
            unknownCards[card] -= 1
        }

        knownDeck[index] = card
    }
}