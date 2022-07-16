//package engine.deck
//
//import engine.card.Card
//
//class FrequenciesDeck(
//    private val deck: Map<Card, Int>,
//    override val discard: List<Card>
//    ): Deck {
//    override fun shuffle() {}
//
//    override fun draw(): Card? {
//        TODO("Not yet implemented")
//    }
//
//    override fun addToDeck(vararg cards: Card) {
//        TODO("Not yet implemented")
//    }
//
//    override fun addToDiscard(vararg cards: Card) {
//        TODO("Not yet implemented")
//    }
//
//    override fun topdeck(card: Card): Card {
//        TODO("Not yet implemented")
//    }
//
//    override fun discard(card: Card): Card {
//        TODO("Not yet implemented")
//    }
//
//    override fun removeFromDiscard(card: Card): Card {
//        TODO("Not yet implemented")
//    }
//
//    override fun copy(): Deck {
//        TODO("Not yet implemented")
//    }
//
//    override fun toListDeck(): ListDeck {
//        TODO("Not yet implemented")
//    }
//
//    override val deckSize: Int
//        get() = deck.values.sum()
//    override val discardSize: Int
//        get() = discard.size
//    override val allCards: List<Card>
//        get() = deck.
//}