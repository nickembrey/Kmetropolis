package engine

typealias CardEffect = (GameState) -> GameState

fun witchEffect(state: GameState): GameState = state.apply {
        if(state.board[Card.CURSE]!! > 0) {
            state.board[Card.CURSE] = state.board[Card.CURSE]!! - 1
            otherPlayer.discard += Card.CURSE
        }
    }

fun militiaEffect(state: GameState): GameState = state.apply {
    state.context = ChoiceContext.MILITIA
    state.choiceCounter = state.choicePlayer.hand.size - 3
}

fun moneylenderEffect(state: GameState): GameState = state.apply {
        if(currentPlayer.hand.contains(Card.COPPER)) {
            currentPlayer.trashCard(Card.COPPER, logger = state.logger)
            currentPlayer.coins += 3
        }
    }

fun chapelEffect(state: GameState): GameState = state.apply {
    state.choiceCounter = 4
    state.context = ChoiceContext.CHAPEL
}

fun workshopEffect(state: GameState): GameState = state.apply {
    state.context = ChoiceContext.WORKSHOP
}