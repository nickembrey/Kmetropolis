package engine.player.cards

import engine.card.Card
import engine.performance.util.CardCountMap

interface PlayerCards {

    companion object {
        fun new(
            board: CardCountMap,
            initialDeck: CardCountMap
        ): PlayerCards = DefaultPlayerCards.new(board, initialDeck)
    }

    fun copy(): PlayerCards

    val allCards: CardCountMap
    val aside: CardCountMap
    val unknownCards: CardCountMap // TODO: do I need this?
    val knownHand: CardCountMap
    val inPlay: CardCountMap
    val knownDeck: MutableMap<Int, Card>
    val knownDiscard: CardCountMap
    val trash: CardCountMap

    var vassalCard: Card?
    val visibleHand: Boolean

    val handCount: Int
    val deckCount: Int
    val discardCount: Int

    fun sample(n: Int): List<Card>

    fun cleanup()
    fun shuffle()

    // puts the hand back into the deck (random position)
    fun redeck()

    // TODO: one unified move method that takes a card and two locations (like before)
    fun draw(card: Card)
    fun play(card: Card) // i.e., play from hand
    fun playFromDiscard(card: Card)
    fun gain(card: Card)
    fun gainToHand(card: Card)
    fun discard(card: Card)
    fun discardFromAside(card: Card)
    fun discardFromDeck(index: Int)
    fun topdeck(card: Card) // puts a card from the discard onto the deck
    fun trash(card: Card)
    fun trashFromDeck(index: Int)

    fun setAside(index: Int)

    fun identify(card: Card, index: Int)
}