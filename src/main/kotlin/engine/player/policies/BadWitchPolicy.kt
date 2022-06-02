package engine.player.policies

import GameState
import engine.*
import engine.player.Player

val badWitchPolicy = fun(
    state: GameState,
    player: Player,
    context: ChoiceContext,
    choice: Choice
): Decision {
    return when(context) {
        ChoiceContext.ACTION -> Decision(choice, context, 0)
        ChoiceContext.BUY -> {
            val goldCards: Int = state.currentPlayer.allCards.filter { it == Card.GOLD }.size
            val witchCards = state.currentPlayer.allCards.filter { it == Card.WITCH }.size
            val provinceCards = state.currentPlayer.allCards.filter { it == Card.PROVINCE }.size

            val witchLeft = state.board[Card.WITCH]!!
            val duchyLeft = state.board[Card.DUCHY]!!
            val estateLeft = state.board[Card.ESTATE]!!


            return if(state.currentPlayer.coins >= 8 && goldCards > 0) {
                Decision(choice, context, choice.indexOf(Card.PROVINCE))
            } else if (state.currentPlayer.coins >= 5 && witchCards == 0 && witchLeft > 0) {
                Decision(choice, context, choice.indexOf(Card.WITCH))
            } else if (state.currentPlayer.coins >= 5 && provinceCards < 4 && duchyLeft > 0) {
                Decision(choice, context, choice.indexOf(Card.DUCHY))
            } else if (state.currentPlayer.coins >= 5 && provinceCards < 2 && estateLeft > 0) {
                Decision(choice, context, choice.indexOf(Card.ESTATE))
            } else if (state.currentPlayer.coins >= 6) {
                Decision(choice, context, choice.indexOf(Card.GOLD))
            } else if (state.currentPlayer.coins >= 3) {
                Decision(choice, context, choice.indexOf(Card.SILVER))
            } else {
                Decision(choice, context, null)
            }
        }
        ChoiceContext.MILITIA -> {
            Decision(choice, context, 0)
        }
        ChoiceContext.WORKSHOP -> {
            throw NotImplementedError()
        }
        ChoiceContext.CHAPEL -> {
            throw NotImplementedError()
        }
    }
}