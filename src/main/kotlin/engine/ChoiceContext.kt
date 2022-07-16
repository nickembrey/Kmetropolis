package engine

import engine.card.Card
import engine.card.CardType
import engine.player.Player

// TODO: consider making CardChoices a Set, and then just choosing a choice token from it rather than an index to a Card
//       -- see note in Card.kt

typealias CardChoices = List<Card?>

// TODO: keep hold of a set in memory and just change it for every choice instead of making a bunch of stuff
enum class ChoiceContext {
    ACTION,
    TREASURE,
    BUY,
    CELLAR,
    CHAPEL,
    HARBINGER,
//    VASSAL,
    WORKSHOP,
    MILITIA,
    POACHER,
    REMODEL_TRASH,
    REMODEL_GAIN;

    fun getCardChoices(player: Player, board: Board): CardChoices {
        return when(this) {
            ACTION -> player.hand.filter { it.type == CardType.ACTION }.toMutableSet<Card?>().apply { add(null) }.toList()
            TREASURE -> player.hand.filter { it.type == CardType.TREASURE }.toMutableSet<Card?>().apply { add(null) }.toList()
            BUY -> board.filter { it.value > 0 && player.coins >= it.key.cost }.keys.toList().plus(null)
            CELLAR, CHAPEL -> player.hand.toMutableSet<Card?>().apply { add(null) }.toList()
            HARBINGER -> player.deck.discard.distinct().ifEmpty { listOf(null) }
//            VASSAL -> player.deck.
            WORKSHOP -> board.filter { it.key.cost < 5 && it.value > 0 }.keys.toList().ifEmpty { listOf(null) }
            MILITIA, POACHER, REMODEL_TRASH -> player.hand.distinct().ifEmpty { listOf(null) }
            REMODEL_GAIN -> {
                board.filter { it.key.cost <= player.remodelCard!!.cost + 2 && it.value > 0 }.keys.takeIf { it.isNotEmpty() }?.toList() ?: listOf(null)
            }
        }
    }

}