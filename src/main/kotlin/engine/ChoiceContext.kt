package engine

// TODO: consider making CardChoices a Set, and then just choosing a choice token from it rather than an index to a Card
//       -- see note in Card.kt

typealias CardChoices = List<Card?>

enum class ChoiceContext {
    ACTION, TREASURE, BUY, CHAPEL, MILITIA, WORKSHOP, REMODEL_TRASH, REMODEL_GAIN;

    fun getCardChoices(player: Player, board: Board): CardChoices {

            return when(this) {
            ACTION -> player.hand.filter { it.type == CardType.ACTION }.distinct().plus(null)
            TREASURE -> player.hand.filter { it.type == CardType.TREASURE }.distinct().plus(null)
            BUY -> board.filter { it.value > 0 && player.coins >= it.key.cost }.keys.toList().plus(null)
            CHAPEL -> player.hand.distinct().plus(null)
            WORKSHOP -> board.filter { it.key.cost < 5 && it.value > 0 }.keys.toList().ifEmpty { listOf(null) }
            MILITIA, REMODEL_TRASH -> player.hand.distinct().ifEmpty { listOf(null) }
            REMODEL_GAIN -> {
                board.filter { it.key.cost < player.remodelCard!!.cost + 2 && it.value > 0 }.keys.takeIf { it.isNotEmpty() }?.toList() ?: listOf(null)
            }
        }.also { if(it.isEmpty()) {
            throw java.lang.IllegalStateException("getCardChoices must provide at least one choice!")
        } }
    }

}