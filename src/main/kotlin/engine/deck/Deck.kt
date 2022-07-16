package engine.deck

import engine.card.Card

// TODO: weird that the deck has the discard inside it, but I don't see a better way...
//       maybe also include the inPlay and hand and call "deck" "drawPile" or something?

// TODO: may want to merge back with player?
interface Deck { // TODO: I don't think this should manage the discard -- maybe just have it be shuffled by the player
                 //       when there are no more cards

    fun shuffle()

    fun draw(): Card?
    // TODO: probably shouldn't be using vararg
    fun addToDeck(vararg cards: Card) // TODO: where is this used?
                                //       seems like we should force it to be used in conjunction with shuffle
    fun addToDiscard(vararg cards: Card)
    fun topdeck(card: Card): Card // TODO: I don't really like that I've forced myself to return Card
    fun discard(card: Card): Card

    fun removeFromDiscard(card: Card): Card // TODO: rethink

    fun copy(): Deck
    fun toListDeck(): ListDeck

    val deckSize: Int
    val discardSize: Int

    val discard: List<Card>
    val allCards: List<Card>
}