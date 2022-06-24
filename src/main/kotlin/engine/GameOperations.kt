package engine

// TODO: this isn't the right place to merge the cardChoices and the decision
fun playActionCard(state: GameState, cardChoices: SingleCardChoices, decision: Decision): GameState {

    val card = cardChoices.choices[decision.index]

    if(state.verbose) {
        println("${state.currentPlayer.name} plays ${card!!.name}")   // TODO:
    }
    state.currentPlayer.hand.remove(card)
    state.currentPlayer.inPlay.add(card!!)   // TODO:
    state.currentPlayer.buys += card.addBuys
    state.currentPlayer.actions -= 1
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

// TODO: eh, just unify with playActionCard again
fun playTreasureCard(state: GameState, cardChoices: SingleCardChoices, decision: Decision): GameState {
    val card = cardChoices.choices[decision.index]

    if(state.verbose) {
        println("${state.currentPlayer.name} plays ${card!!.name}")   // TODO:
    }
    state.currentPlayer.hand.remove(card)
    state.currentPlayer.inPlay.add(card!!) // TODO:
    state.currentPlayer.buys += card.addBuys
    state.currentPlayer.actions -= 1
    state.currentPlayer.coins += card.addCoins
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

fun endTurn(state: GameState) {
    val currentPlayer = state.currentPlayer
    currentPlayer.discard += currentPlayer.inPlay
    currentPlayer.inPlay = mutableListOf()
    currentPlayer.discard += currentPlayer.hand
    currentPlayer.hand = mutableListOf()
    for (i in 1..5) {
        drawCard(currentPlayer, !state.noShuffle)
    }
    currentPlayer.buys = 1
    currentPlayer.coins = 0
    currentPlayer.actions = 1
    state.turns += 1
    state.currentPlayer = state.otherPlayer
    state.context = ChoiceContext.ACTION
}

fun buyCard(state: GameState, card: Card, verbose: Boolean = false) {
    if(state.verbose) {
        println("${state.currentPlayer.name} buys ${card.name}")
    }
    state.currentPlayer.coins -= card.cost
    state.currentPlayer.buys -= 1
    state.board[card] = state.board[card]!! - 1
    gainCard(state.currentPlayer, card, verbose)
}

fun decideGainCard(currentPlayer: Player, choices: SingleCardChoices, index: Decision, verbose: Boolean = false) {
    val card = choices.choices[index.index]

    gainCard(currentPlayer, card!!, verbose)   // TODO:
}

fun gainCard(currentPlayer: Player, card: Card, verbose: Boolean = false) {
    if(verbose) {
        println("${currentPlayer.name} gains ${card.name}")
    }
    currentPlayer.discard.add(card)
}

fun trashCards(currentPlayer: Player, choices: MultipleCardChoices, index: Decision, verbose: Boolean = false) {
// TODO: there should be an empty choice whenever this method is reached
    val cards = choices.choices[index.index]
    for(card in cards) {
        trashCard(currentPlayer, card!!, verbose = verbose)   // TODO:
    }
}

fun trashCard(currentPlayer: Player, card: Card, verbose: Boolean = false) {
    if(verbose) {
        println("${currentPlayer.name} trashes ${card.name}")
    }
    currentPlayer.hand.remove(card)
}

fun discardCards(currentPlayer: Player, choices: MultipleCardChoices, decision: Decision, verbose: Boolean = false) {
    val cards = choices.choices[decision.index]
    for(card in cards) {
        discardCard(currentPlayer, card, verbose)   // TODO:
    }
}

fun discardCard(currentPlayer: Player, card: Card, verbose: Boolean = false) {
    if(verbose) {
        println("${currentPlayer.name} discards ${card.name}")
    }
    currentPlayer.hand.remove(card)
    currentPlayer.discard.add(card)
}