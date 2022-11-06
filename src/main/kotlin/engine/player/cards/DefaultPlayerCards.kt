package engine.player.cards

import engine.branch.ActionSelection
import engine.branch.BranchSelection
import engine.branch.SpecialBranchSelection
import engine.branch.TreasureSelection
import engine.card.Card
import engine.card.CardType
import engine.performance.util.CardCountMap
import java.util.*

class DefaultPlayerCards private constructor( // TODO: buys seem to be being added to deck
    private val board: Map<Card, Int>,
    override var deck: ArrayList<Card>,
    override var discard: ArrayList<Card> = ArrayList(),
    override var hand: ArrayList<Card> = ArrayList(),
    override var inPlay: ArrayList<Card> = ArrayList(),
    override var trash: ArrayList<Card> = ArrayList(),
    private var topdeckCard: Card? = null
): PlayerCards {

    init {
        discard.ensureCapacity(50)
        hand.ensureCapacity(20)
        inPlay.ensureCapacity(20)
        trash.ensureCapacity(100)
    }

    companion object {
        fun new(
            board: Map<Card, Int>,
            deck: ArrayList<Card>
        ) = DefaultPlayerCards(board, deck)
    }

    override val deckSize: Int
        get() = deck.size

    override val cardCount: Int
        get() = allCards.size

    override val allCards: ArrayList<Card> = when(topdeckCard) {
        null -> (deck + discard + hand + inPlay)
        else -> (listOf(topdeckCard!!) + deck + discard + hand + inPlay)
    }.let { ArrayList(it) }.apply { ensureCapacity(100) }

    override val actionMenu: MutableCollection<BranchSelection> = ArrayList<BranchSelection>(hand.filter { it.type == CardType.ACTION }.map { ActionSelection(card = it) }
        .plus(SpecialBranchSelection.SKIP)).apply { ensureCapacity(20) }
    override val treasureMenu: MutableCollection<BranchSelection> = ArrayList<BranchSelection>(hand.filter { it.type == CardType.TREASURE }.map { TreasureSelection(cards = listOf(it)) }
        .plus(SpecialBranchSelection.SKIP)).apply { ensureCapacity(20) }

    override fun shuffle() {
        deck += discard
        discard.clear()
    }

    override fun shuffle(card: Card) {
        discard.remove(card)
        deck += card
    }

    override fun draw(card: Card) {
        when(card.type) {
            CardType.ACTION -> actionMenu += ActionSelection(card = card)
            CardType.TREASURE -> treasureMenu += TreasureSelection(cards = listOf(card))
            else -> {}
        }
        deck -= card
        hand += card
    }

    override fun undoDraw(card: Card) {
        when(card.type) {
            CardType.ACTION -> actionMenu -= ActionSelection(card = card)
            CardType.TREASURE -> treasureMenu -= TreasureSelection(cards = listOf(card))
            else -> {}
        }
        deck += card
        hand -= card
    }

    override fun play(card: Card) {
        when(card.type) {
            CardType.ACTION -> actionMenu -= ActionSelection(card = card)
            CardType.TREASURE -> treasureMenu -= TreasureSelection(cards = listOf(card))
            else -> {}
        }
        hand -= card
        inPlay += card
    }

    override fun gain(card: Card) {
        allCards += card
        discard += card
    }

    override fun cleanup(card: Card) {
        inPlay -= card
        discard += card
    }

    override fun discard(card: Card) {
        when(card.type) {
            CardType.ACTION -> actionMenu -= ActionSelection(card = card)
            CardType.TREASURE -> treasureMenu -= TreasureSelection(cards = listOf(card))
            else -> {}
        }
        hand -= card
        discard += card
    }

    override fun topdeck(card: Card) {
        topdeckCard = card
    }

    override fun trash(card: Card) {
        when(card.type) {
            CardType.ACTION -> actionMenu -= ActionSelection(card = card)
            CardType.TREASURE -> treasureMenu -= TreasureSelection(cards = listOf(card))
            else -> {}
        }
        allCards -= card
        hand -= card
        trash += card
    }

    override fun toCardFrequencyMap(board: Map<Card, Int>): Map<Card, Int> {
        val map = EnumMap(board.keys.associateWith { 0 }.toMutableMap())
        allCards.forEach {
            map.merge(it, 1, Int::plus)
        }
        return map
    }

    override fun copyCards(): DefaultPlayerCards =
        DefaultPlayerCards(
            board = board.toMap(),
            deck = ArrayList(deck),
            discard = ArrayList(discard),
            hand = ArrayList(hand),
            inPlay = ArrayList(inPlay),
            trash = ArrayList(trash),
            topdeckCard = topdeckCard)
}