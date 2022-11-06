package engine.player.cards

import engine.branch.BranchSelection
import engine.card.Card

interface PlayerCards {

    companion object {
        fun new(
            board: Map<Card, Int>,
            deck: ArrayList<Card> = ArrayList(
                listOf(Card.ESTATE, Card.ESTATE, Card.ESTATE,
                    Card.COPPER, Card.COPPER, Card.COPPER, Card.COPPER,
                    Card.COPPER, Card.COPPER, Card.COPPER)
            )
        ): PlayerCards = DefaultPlayerCards.new(board, deck)
    }

    val deckSize: Int
    val deck: List<Card>
    val discard: List<Card>
    val hand: List<Card>
    val inPlay: List<Card>
    val trash: List<Card>

    val cardCount: Int
    val allCards: List<Card>

    val actionMenu: Collection<BranchSelection>
    val treasureMenu: Collection<BranchSelection>

    // TODO: need undo for each operation and need to put shuffling onto the stack
    //       before we can use undo on GameState

    fun shuffle() // TODO: get rid of in favor of below

    fun shuffle(card: Card)
    fun draw(card: Card)
    fun undoDraw(card: Card)
    fun play(card: Card)
    fun gain(card: Card)
    fun cleanup(card: Card)
    fun discard(card: Card)
    fun topdeck(card: Card)
    fun trash(card: Card)

    fun toCardFrequencyMap(board: Map<Card, Int>): Map<Card, Int>

    fun copyCards(): PlayerCards
}