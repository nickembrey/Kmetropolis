package engine

import engine.card.Card
import engine.card.CardType
import engine.player.Player

// TODO: consider making CardChoices a Set, and then just choosing a choice token from it rather than an index to a Card
//       -- see note in Card.kt

typealias CardChoices = List<Card?>

enum class ChoiceContext {
    ACTION, TREASURE, BUY, CELLAR, CHAPEL, HARBINGER, WORKSHOP, MILITIA, REMODEL_TRASH, REMODEL_GAIN;

    fun getCardChoices(player: Player, board: Board, distinct: Boolean = true): CardChoices {

            if(distinct) {
                return when(this) {
                    ACTION -> player.hand.filter { it.type == CardType.ACTION }.toMutableSet<Card?>().apply { add(null) }.toList()
                    TREASURE -> player.hand.filter { it.type == CardType.TREASURE }.toMutableSet<Card?>().apply { add(null) }.toList()
                    BUY -> board.filter { it.value > 0 && player.coins >= it.key.cost }.keys.toList().plus(null)
                    CELLAR, CHAPEL -> player.hand.toMutableSet<Card?>().apply { add(null) }.toList()
                    HARBINGER -> player.discard.distinct().ifEmpty { listOf(null) }
                    WORKSHOP -> board.filter { it.key.cost < 5 && it.value > 0 }.keys.toList().ifEmpty { listOf(null) }
                    MILITIA, REMODEL_TRASH -> player.hand.distinct().ifEmpty { listOf(null) }
                    REMODEL_GAIN -> {
                        board.filter { it.key.cost <= player.remodelCard!!.cost + 2 && it.value > 0 }.keys.takeIf { it.isNotEmpty() }?.toList() ?: listOf(null)
                    }
                }.also { if(it.isEmpty()) {
                    throw java.lang.IllegalStateException("getCardChoices must provide at least one choice!")
                } }
            } else {
                val choices: MutableList<Card?> = mutableListOf()
                return when(this) {
                    ACTION -> player.hand.filterTo(choices) { it.type == CardType.ACTION }.apply { add(null) }
                    TREASURE -> player.hand.filterTo(choices) { it.type == CardType.TREASURE }.apply { add(null) }
                    BUY -> board.filter { it.value > 0 && player.coins >= it.key.cost }.keys.toList().plus(null)
                    CELLAR, CHAPEL -> player.hand.plus(null)
                    HARBINGER -> player.discard.ifEmpty { listOf(null) }
                    WORKSHOP -> board.filter { it.key.cost < 5 && it.value > 0 }.keys.toList().ifEmpty { listOf(null) }
                    MILITIA, REMODEL_TRASH -> player.hand.ifEmpty { listOf(null) }
                    REMODEL_GAIN -> {
                        board.filter { it.key.cost <= player.remodelCard!!.cost + 2 && it.value > 0 }.keys.takeIf { it.isNotEmpty() }?.toList() ?: listOf(null)
                    }
                }.also { if(it.isEmpty()) {
                    throw java.lang.IllegalStateException("getCardChoices must provide at least one choice!")
                } }
            }
    }

}