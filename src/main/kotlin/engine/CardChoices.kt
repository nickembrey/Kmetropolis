package engine

import util.combinations

interface CardChoices { // TODO: add getting card function here
    val choices: List<*> // TODO: I wonder if this could be any better...
    fun isEmpty(): Boolean = choices.isEmpty()
    fun isNotEmpty(): Boolean = choices.isNotEmpty()
}

// TODO: note that null is used for picking no card

@JvmInline
value class SingleCardChoices(override val choices: List<Card?> = listOf()): CardChoices

@JvmInline
value class MultipleCardChoices(override val choices: List<List<Card>> = listOf()): CardChoices
// TODO: consider changing to a set
// TODO: make sure you can always choose no card or a list of empty cards

// TODO: do we need player?
fun getCardChoices(state: GameState, player: Player, context: ChoiceContext): CardChoices {
    return when(context) {
        ChoiceContext.ACTION -> {
            if(player.actions > 0) {
                val actionCards = player.hand.filter { it.type == CardType.ACTION }.distinct()
                if(actionCards.isNotEmpty()) {
                    return SingleCardChoices(actionCards + listOf(null))
                }
            }
            return SingleCardChoices()
        }
        ChoiceContext.TREASURE -> SingleCardChoices(player.hand.filter { it.type == CardType.TREASURE }.distinct() + listOf(null))
        ChoiceContext.BUY -> {
            if(player.buys > 0) {
                SingleCardChoices(state.board.filter { it.value > 0 && player.coins >= it.key.cost }.keys.toList() + listOf(null))
            } else {
                SingleCardChoices()
            }
        }
        ChoiceContext.MILITIA -> {
            if(player.hand.size > 3) {
                MultipleCardChoices(player.hand.combinations(player.hand.size - 3).toList())
            } else {
                MultipleCardChoices()
            }
        }
        ChoiceContext.WORKSHOP -> SingleCardChoices(state.board.filter { it.key.cost < 5 && it.value > 0 }.keys.toList())
        ChoiceContext.CHAPEL -> MultipleCardChoices(( player.hand.combinations(4) +
                player.hand.combinations(3) +
                player.hand.combinations(2) +
                player.hand.combinations(1)).toList())
    }
}