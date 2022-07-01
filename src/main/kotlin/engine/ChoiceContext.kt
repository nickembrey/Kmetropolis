package engine

// TODO: consider making CardChoices a Set, and then just choosing a choice token from it rather than an index to a Card
//       -- see note in Card.kt

typealias CardChoices = List<Card?>

enum class ChoiceContext {
    ACTION, TREASURE, BUY, CHAPEL, MILITIA, WORKSHOP, REMODEL_TRASH, REMODEL_GAIN;

    fun getCardChoices(player: Player, board: Board): CardChoices {

        return when(this) {
            ACTION -> player.hand.filter { it.type == CardType.ACTION }.distinct() + listOf(null)
            TREASURE -> player.hand.filter { it.type == CardType.TREASURE }.distinct() + listOf(null)
            BUY -> board.filter { it.value > 0 && player.coins >= it.key.cost }.keys.toList() + listOf(null)
            CHAPEL -> player.hand + listOf(null)
            MILITIA -> player.hand.takeIf { it.isNotEmpty() }?.distinct() ?: listOf(null)
            WORKSHOP -> board.filter { it.key.cost < 5 && it.value > 0 }.keys.toList().takeIf { it.isNotEmpty() } ?: listOf(null)
            REMODEL_TRASH -> player.hand.takeIf { it.isNotEmpty() }?.distinct() ?: listOf(null)
            REMODEL_GAIN -> {
                board.filter { it.key.cost < player.remodelCard!!.cost + 2 && it.value > 0 }.keys.toList().takeIf { it.isNotEmpty() } ?: listOf(null)
            }
        }.also { if(it.isEmpty()) {
            throw java.lang.IllegalStateException("getCardChoices must provide at least one choice!")
        }}
    }

}