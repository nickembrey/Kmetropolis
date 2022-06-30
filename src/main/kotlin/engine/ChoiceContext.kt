package engine

// TODO: consider making CardChoices a Set, and then just choosing a choice token from it rather than an index to a Card
//       -- see note in Card.kt

typealias CardChoices = List<Card?>

enum class ChoiceContext {
    ACTION, TREASURE, BUY, CHAPEL, MILITIA, WORKSHOP;

    fun getCardChoices(player: Player, board: Board): CardChoices {

        return when(this) {
            ACTION -> player.hand.filter { it.type == CardType.ACTION } + listOf(null)
            TREASURE -> player.hand.filter { it.type == CardType.TREASURE } + listOf(null)
            BUY -> board.filter { it.value > 0 && player.coins >= it.key.cost }.keys.toList() + listOf(null)
            CHAPEL -> player.hand + listOf(null)
            MILITIA -> player.hand.takeIf { it.isNotEmpty() } ?: listOf(null)
            WORKSHOP -> board.filter { it.key.cost < 5 && it.value > 0 }.keys.toList().takeIf { it.isNotEmpty() } ?: listOf(null)
        }
    }

}