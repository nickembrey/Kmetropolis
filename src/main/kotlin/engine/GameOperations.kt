package engine

import GameState
import engine.player.Player

fun playCard(state: GameState, decision: Decision): GameState {
    val card = decision.choice[decision.index!!] as Card
    state.currentPlayer.hand.remove(card)
    state.currentPlayer.inPlay.add(card)
    state.currentPlayer.buys += card.addBuys
    state.currentPlayer.actions += card.addActions
    state.currentPlayer.coins += card.addCoins
    for(draw in 1..card.addCards) {
        drawCard(state.currentPlayer, !state.noShuffle)
    }
    for(effect in card.effectList) {
        effect.activate(state)
    }
    return state
}

fun drawCard(currentPlayer: Player, trueShuffle: Boolean = true) {
    if(currentPlayer.deck.size == 0) {
        shuffle(currentPlayer, trueShuffle)
    }
    if(currentPlayer.deck.size > 0) {
        currentPlayer.hand.add(currentPlayer.deck[0])
        currentPlayer.deck.removeAt(0)
    }
}

fun shuffle(currentPlayer: Player, trueShuffle: Boolean = true) {
    currentPlayer.deck += currentPlayer.discard
    currentPlayer.discard = mutableListOf()
    if(trueShuffle) {
        currentPlayer.deck.shuffle()
    }
}

fun buyCards(state: GameState, decision: Decision) {
    val cards = decision.choice[decision.index!!] as Map<Card, Int> // TODO:
    val candidates = cards.filter { it.value > 0 }
    for((card, number) in candidates) {
        for(it in 1..number) {
            buyCard(state, card)
        }
    }

}

fun buyCard(state: GameState, card: Card) {
    state.currentPlayer.coins -= card.cost
    state.currentPlayer.buys -= 1
    state.board[card] = state.board[card]!! - 1
    gainCard(state.currentPlayer, card)
}

fun decideGainCard(currentPlayer: Player, decision: Decision) {
    val card = decision.choice[decision.index!!] as Card
    gainCard(currentPlayer, card)
}

fun gainCard(currentPlayer: Player, card: Card) {
    currentPlayer.discard.add(card)
}

fun trashCards(currentPlayer: Player, decision: Decision) {
    if(decision.index != null) {
        val cards = decision.choice[decision.index] as List<Card>
        for(card in cards) {
            trashCard(currentPlayer, card)
        }
    }
}

fun trashCard(currentPlayer: Player, card: Card) {
    currentPlayer.hand.remove(card)
}

fun discardCards(currentPlayer: Player, decision: Decision) {
    val cards = decision.choice[decision.index!!] as List<Card>
    for(card in cards) {
        currentPlayer.hand.remove(card)
        currentPlayer.discard.add(card)
    }
}