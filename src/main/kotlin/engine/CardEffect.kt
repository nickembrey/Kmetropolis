package engine

typealias CardEffect = (GameState) -> GameState

fun witchEffect(state: GameState): GameState = state.apply {
        if(board[Card.CURSE]!! > 0) {
            board[Card.CURSE] = board[Card.CURSE]!! - 1
            otherPlayer.discard += Card.CURSE
        }
    }

fun militiaEffect(state: GameState): GameState = state.apply {
    context = ChoiceContext.MILITIA
}

fun moneylenderEffect(state: GameState): GameState = state.apply {
        if(currentPlayer.hand.contains(Card.COPPER)) {
            currentPlayer.trashCard(Card.COPPER, logger = logger)
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