package engine

import GameState
import engine.player.Player
import util.combinations

enum class ChoiceContext {
    ACTION, BUY, CHAPEL, MILITIA, WORKSHOP;

    companion object {
        fun getContext(turnPhase: TurnPhase): ChoiceContext = when(turnPhase) {
            TurnPhase.ACTION -> ACTION
            TurnPhase.BUY -> BUY
        }
    }

    fun getChoice(state: GameState, player: Player): Choice {
        return when(this) {
            ACTION -> player.hand.filter { it.type == CardType.ACTION }
            BUY -> getBuyChoice(state, player)
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
                    player.hand.combinations(1)).toMutableList().apply { listOf<Card>() }
        }
    }
}

fun getBuyChoice(state: GameState, player: Player): Choice {
    val orders = mutableSetOf<Map<Card, Int>>()

    val initialMenu = state.board.toMutableMap()
    val initialOrder = state.board.keys.zip(MutableList(state.board.size) { 0 })
        .toMap().toMutableMap()

    fun pick(
        menu: MutableMap<Card, Int>,
        order: MutableMap<Card, Int>,
        coins: Int,
        buys: Int
    ) {
        orders.add(order.toMutableMap())
        if (buys > 0) {
            val available = menu.filter { it.value > 0 && coins >= it.key.cost }.keys
            for (card in available) {
                pick(
                    menu.apply { set(card, getValue(card) - 1) },
                    order.apply { set(card, getValue(card) + 1) },
                    coins - card.cost,
                    buys - 1
                )
                menu.apply { set(card, getValue(card) + 1) }
                order.apply { set(card, getValue(card) - 1) }
            }
        }

    }

    pick(initialMenu, initialOrder, player.coins, player.buys)

    return orders.toList()

}