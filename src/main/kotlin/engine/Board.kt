package engine

import engine.card.Card

// TODO: we could also change it to just use the ordinal instead of the whole card
typealias Board = HashMap<Card, Int>

fun Board.removeCard(card: Card): Card? {
    return if(getValue(card) > 0) {
        card.also { merge(card, 1, Int::minus) }
    } else {
        null
    }
}

fun Board.addCard(card: Card): Card {
    return card.also { merge(card, 1, Int::plus) }
}