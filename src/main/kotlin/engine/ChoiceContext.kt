package engine

import util.combinations

enum class ChoiceContext {
    ACTION, TREASURE, BUY, CHAPEL, MILITIA, WORKSHOP;

    fun getCardChoices(state: GameState, player: Player): CardChoices {
        return when(this) {
            ACTION -> if(player.actions > 0) {
                SingleCardChoices(player.hand.filter { it.type == CardType.ACTION })
            } else {
                SingleCardChoices()
            }
            TREASURE -> SingleCardChoices(player.hand.filter { it.type == CardType.TREASURE })
            BUY -> SingleCardChoices(state.board.filter { it.value > 0 && player.coins >= it.key.cost }.keys.toList())
            MILITIA -> {
                if(player.hand.size > 3) {
                    MultipleCardChoices(player.hand.combinations(player.hand.size - 3).toList())
                } else {
                    MultipleCardChoices()
                }
            }
            WORKSHOP -> SingleCardChoices(state.board.filter { it.key.cost < 5 && it.value > 0 }.keys.toList())
            CHAPEL -> MultipleCardChoices(( player.hand.combinations(4) +
                    player.hand.combinations(3) +
                    player.hand.combinations(2) +
                    player.hand.combinations(1)).toList())
        }
    }
}