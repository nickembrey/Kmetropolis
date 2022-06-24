package engine

import util.combinations

enum class ChoiceContext {
    ACTION, BUY, CHAPEL, MILITIA, WORKSHOP;

    fun getChoice(state: GameState, player: Player): Choice {
        return when(this) {
            ACTION -> if(player.actions > 0) {
                player.hand.filter { it.type == CardType.ACTION }
            } else {
                listOf()
            }
            BUY -> state.board.filter { it.value > 0 && player.coins >= it.key.cost }.keys.toList()
            MILITIA -> {
                if(player.hand.size > 3) {
                    player.hand.combinations(player.hand.size - 3).toList()
                } else {
                    listOf()
                }
            }
            WORKSHOP -> state.board.filter { it.key.cost < 5 && it.value > 0 }.keys.toList()
            CHAPEL -> ( player.hand.combinations(4) +
                    player.hand.combinations(3) +
                    player.hand.combinations(2) +
                    player.hand.combinations(1)).toList()
        }
    }
}