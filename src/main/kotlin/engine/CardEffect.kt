package engine

typealias CardEffect = (GameState) -> GameState

fun witchEffect(state: GameState): GameState = state.apply {
    moveCard(Card.CURSE, CardLocation.SUPPLY, CardLocation.PLAYER_TWO_DISCARD)
}

fun militiaEffect(state: GameState): GameState = state.apply {
    context = ChoiceContext.MILITIA
}

fun moneylenderEffect(state: GameState): GameState = state.apply {
        if(currentPlayer.hand.contains(Card.COPPER)) {
            moveCard(Card.COPPER, currentPlayer.handLocation, CardLocation.TRASH)
            currentPlayer.coins += 3
        }
    }

fun chapelEffect(state: GameState): GameState = state.apply {
    context = ChoiceContext.CHAPEL
    contextDecisionCounters = 4
}

fun workshopEffect(state: GameState): GameState = state.apply {
    context = ChoiceContext.WORKSHOP
    contextDecisionCounters = 1
}

fun remodelEffect(state: GameState): GameState = state.apply {
    context = ChoiceContext.REMODEL_TRASH
    contextDecisionCounters = 1
}