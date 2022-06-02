package engine

import GameState

enum class CardEffect(val activate: (GameState) -> GameState) {
    WitchEffect( fun (state: GameState): GameState = state.apply {
        if(state.board[Card.CURSE]!! > 0) {
            state.board[Card.CURSE] = state.board[Card.CURSE]!! - 1
            otherPlayer.discard += Card.CURSE
        }
    }),
    MilitiaEffect( fun (state: GameState): GameState = state.apply {
        state.context = engine.ChoiceContext.MILITIA
    }),
    MoneylenderEffect( fun (state: GameState): GameState = state.apply {
        if(currentPlayer.hand.contains(Card.COPPER)) {
            trashCard(currentPlayer, Card.COPPER, verbose = state.verbose)
            currentPlayer.coins += 3
        }
    }),
    ChapelEffect( fun (state: GameState): GameState = state.apply {
        state.context = engine.ChoiceContext.CHAPEL
    }),
    WorkshopEffect( fun (state: GameState): GameState = state.apply {
        state.context = engine.ChoiceContext.WORKSHOP
    })
}