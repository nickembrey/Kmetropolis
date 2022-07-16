package engine.deck

import engine.card.Card
import engine.card.prepend

open class ListDeck(
    private val deck: MutableList<Card> = mutableListOf(
        Card.COPPER,
        Card.COPPER,
        Card.COPPER,
        Card.COPPER,
        Card.COPPER,
        Card.COPPER,
        Card.COPPER,
        Card.ESTATE,
        Card.ESTATE,
        Card.ESTATE),
    override val discard: MutableList<Card> = mutableListOf()
): Deck {

    fun toDeterministicDeck(): DeterministicDeck {
        return DeterministicDeck(deck, discard)
    }

    override fun shuffle() {
        deck += discard
        discard.clear()
        deck.shuffle()
    }

    override fun draw(): Card? {
        if (deck.size == 0) {
            shuffle()
        }
        return deck.removeFirstOrNull()
    }

    override fun addToDeck(vararg cards: Card) {
        deck.addAll(cards)
    }

    override fun addToDiscard(vararg cards: Card) {
        discard.addAll(cards)
    }

    override fun topdeck(card: Card): Card {
        deck.prepend(card)
        return card
    }

    override fun discard(card: Card): Card {
        discard.add(card)
        return card
    }

    override fun removeFromDiscard(card: Card): Card {
        val removed = discard.remove(card)
        if(!removed) {
            throw IllegalStateException("Could not find card in discard!")
        }
        return card
    }

    override fun copy(): Deck {
        return ListDeck(
            deck = deck.toMutableList(),
            discard = discard.toMutableList()
        )
    }

    // TODO: how to not reuse?
    override fun toListDeck(): ListDeck {
        return ListDeck(
            deck = deck.toMutableList(),
            discard = discard.toMutableList()
        )
    }

    override val deckSize: Int
        get() = deck.size

    override val discardSize: Int
        get() = discard.size

    override val allCards: List<Card>
        get() = deck + discard

}