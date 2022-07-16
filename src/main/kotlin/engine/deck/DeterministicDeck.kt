package engine.deck

import engine.card.Card
import engine.card.prepend

class DeterministicDeck(
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
    discard: MutableList<Card> = mutableListOf()
): ListDeck(deck, discard), Deck {

    override fun shuffle() {
        deck += discard
        discard.clear()
    }

    override fun copy(): Deck {
        return DeterministicDeck(
            deck = deck.toMutableList(),
            discard = discard.toMutableList()
        )
    }

}