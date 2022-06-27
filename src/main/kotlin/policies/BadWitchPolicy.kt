package policies

import engine.*
import engine.Player

fun badWitchPolicy(
    state: GameState,
    player: Player, // TODO: does player ever get used by any policy?
    context: ChoiceContext,
    cardChoices: CardChoices
): DecisionIndex {
    return when(context) {
        ChoiceContext.ACTION -> 0
        ChoiceContext.TREASURE -> 0
        ChoiceContext.BUY -> {
            val goldCards: Int = state.currentPlayer.allCards.filter { it == Card.GOLD }.size
            val witchCards = state.currentPlayer.allCards.filter { it == Card.WITCH }.size
            val provinceCards = state.currentPlayer.allCards.filter { it == Card.PROVINCE }.size

            val witchLeft = state.board[Card.WITCH]!!
            val duchyLeft = state.board[Card.DUCHY]!!
            val estateLeft = state.board[Card.ESTATE]!!


            return if(state.currentPlayer.coins >= 8 && goldCards > 0) {
                cardChoices.indexOf(Card.PROVINCE)
            } else if (state.currentPlayer.coins >= 5 && witchCards == 0 && witchLeft > 0) {
                cardChoices.indexOf(Card.WITCH)
            } else if (state.currentPlayer.coins >= 5 && provinceCards < 4 && duchyLeft > 0) {
                cardChoices.indexOf(Card.DUCHY)
            } else if (state.currentPlayer.coins >= 5 && provinceCards < 2 && estateLeft > 0) {
                cardChoices.indexOf(Card.ESTATE)
            } else if (state.currentPlayer.coins >= 6) {
                cardChoices.indexOf(Card.GOLD)
            } else if (state.currentPlayer.coins >= 3) {
                cardChoices.indexOf(Card.SILVER)
            } else {
                cardChoices.indexOf(null)
            }
        }
        ChoiceContext.MILITIA -> 0
        ChoiceContext.WORKSHOP -> throw NotImplementedError()
        ChoiceContext.CHAPEL -> throw NotImplementedError()
    }
}