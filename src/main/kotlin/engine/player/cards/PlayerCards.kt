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
    val unknownCards: CardCountMap // TODO: do I need this?
    val knownHand: CardCountMap
    val inPlay: CardCountMap
    val knownDeck: MutableMap<Int, Card>
    val knownDiscard: CardCountMap
    val trash: CardCountMap

    val visibleHand: Boolean

    val handCount: Int
    val deckCount: Int
    val discardCount: Int

    fun sample(n: Int): List<Card>

    fun cleanup()
    fun shuffle()

    // puts the hand back into the deck (random position)
    fun redeck()


    fun draw(card: Card)
    fun play(card: Card)
    fun gain(card: Card)
    fun discard(card: Card)
    fun topdeck(card: Card) // puts a card from the discard onto the deck
    fun trash(card: Card)

    fun identify(card: Card, index: Int)
}