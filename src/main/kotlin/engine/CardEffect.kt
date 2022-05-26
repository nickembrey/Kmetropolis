package engine

import GameState

enum class CardEffect(val activate: (GameState) -> GameState) {
    WitchEffect( fun (state: GameState): GameState = state.apply {
        if(state.board[engine.Card.CURSE]!! > 0) {
            state.board[engine.Card.CURSE] = state.board[engine.Card.CURSE]!! - 1
            otherPlayer.discard += engine.Card.CURSE
        }
    }),
    MilitiaEffect( fun (state: GameState): GameState = state.apply {
        val decision = otherPlayer.getDecision(state, engine.ChoiceContext.MILITIA)
        otherPlayer.makeDecision(state, ChoiceContext.MILITIA, decision)
    }),
    MoneylenderEffect( fun (state: GameState): GameState = state.apply {
        if(currentPlayer.hand.contains(Card.COPPER)) {
            trashCard(currentPlayer, Card.COPPER)
            currentPlayer.coins += 3
        }
    }),
    ChapelEffect( fun (state: GameState): GameState = state.apply {
        val decision = currentPlayer.getDecision(state, engine.ChoiceContext.CHAPEL)
        currentPlayer.makeDecision(state, ChoiceContext.CHAPEL, decision)
    }),
    WorkshopEffect( fun (state: GameState): GameState = state.apply {
        val decision = currentPlayer.getDecision(state, engine.ChoiceContext.WORKSHOP)
        currentPlayer.makeDecision(state, ChoiceContext.WORKSHOP, decision)
    })
}