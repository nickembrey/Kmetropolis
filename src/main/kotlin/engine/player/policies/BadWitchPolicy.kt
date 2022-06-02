package engine.player

import GameState
import engine.*

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

            val noBuy = state.board.keys.zip(MutableList(state.board.size) { 0 }).toMap()
            val provinceBuy = noBuy.toMutableMap().apply { this[Card.PROVINCE] = 1 }
            val witchBuy = noBuy.toMutableMap().apply { this[Card.WITCH] = 1 }
            val goldBuy = noBuy.toMutableMap().apply { this[Card.GOLD] = 1 }
            val duchyBuy = noBuy.toMutableMap().apply { this[Card.DUCHY] = 1 }
            val estateBuy = noBuy.toMutableMap().apply { this[Card.ESTATE] = 1 }
            val silverBuy = noBuy.toMutableMap().apply { this[Card.SILVER] = 1 }

            val witchLeft = state.board[Card.WITCH]!!
            val duchyLeft = state.board[Card.DUCHY]!!
            val estateLeft = state.board[Card.ESTATE]!!


            return if(state.currentPlayer.coins >= 8 && goldCards > 0) {
                Decision(choice, context, choice.indexOf(provinceBuy))
            } else if (state.currentPlayer.coins >= 5 && witchCards == 0 && witchLeft > 0) {
                Decision(choice, context, choice.indexOf(witchBuy))
            } else if (state.currentPlayer.coins >= 5 && provinceCards < 4 && duchyLeft > 0) {
                Decision(choice, context, choice.indexOf(duchyBuy))
            } else if (state.currentPlayer.coins >= 5 && provinceCards < 2 && estateLeft > 0) {
                Decision(choice, context, choice.indexOf(estateBuy))
            } else if (state.currentPlayer.coins >= 6) {
                Decision(choice, context, choice.indexOf(goldBuy))
            } else if (state.currentPlayer.coins >= 3) {
                Decision(choice, context, choice.indexOf(silverBuy))
            } else {
                Decision(choice, context, choice.indexOf(noBuy))
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