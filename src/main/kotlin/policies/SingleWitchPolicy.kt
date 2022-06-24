package policies

import engine.*

val singleWitchPolicy = fun(state: GameState, player: Player, context: ChoiceContext, choice: Choice): Decision {
    return when(context) {
        ChoiceContext.ACTION -> Decision(choice, context, 0)
        ChoiceContext.BUY -> {
            val goldCards: Int = state.currentPlayer.allCards.filter { it == Card.GOLD }.size
            val witchCards = state.currentPlayer.allCards.filter { it == Card.WITCH }.size

            val noBuy = state.board.keys.zip(MutableList(state.board.size) { 0 }).toMap()
            val provinceBuy = noBuy.toMutableMap().apply { this[Card.PROVINCE] = 1 }
            val witchBuy = noBuy.toMutableMap().apply { this[Card.WITCH] = 1 }
            val goldBuy = noBuy.toMutableMap().apply { this[Card.GOLD] = 1 }
            val silverBuy = noBuy.toMutableMap().apply { this[Card.SILVER] = 1 }


            return if(state.currentPlayer.coins >= 8 && goldCards > 0) {
                Decision(choice, context, choice.indexOf(provinceBuy))
            } else if (state.currentPlayer.coins >= 5 && witchCards == 0) {
                Decision(choice, context, choice.indexOf(witchBuy))
            } else if (state.currentPlayer.coins >= 6) {
                Decision(choice, context, choice.indexOf(goldBuy))
            } else if (state.currentPlayer.coins >= 3) {
                Decision(choice, context, choice.indexOf(silverBuy))
            } else {
                Decision(choice, context, choice.indexOf(noBuy))
            }
        }
        ChoiceContext.MILITIA -> {
            throw NotImplementedError()
        }
        ChoiceContext.WORKSHOP -> {
            throw NotImplementedError()
        }
        ChoiceContext.CHAPEL -> {
            throw NotImplementedError()
        }
    }
}