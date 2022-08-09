package engine

import engine.card.Card

interface CardMap<T> {

    operator fun get(card: Card): T
    operator fun set(card: Card, value: T)
}