package engine

enum class ChoiceContext {
    ACTION, TREASURE, BUY, CHAPEL, MILITIA, WORKSHOP;

    fun getCardChoices(player: Player, board: Board): CardChoices {

        return when(this) {
            ACTION -> {
                if(player.actions > 0) {
                    val actionCards = player.hand.filter { it.type == CardType.ACTION }.distinct()
                    if(actionCards.isNotEmpty()) {
                        return actionCards + listOf(null)
                    }
                }
                return listOf(null)
            }
            TREASURE -> player.hand.filter { it.type == CardType.TREASURE }.distinct() + listOf(null)
            BUY -> {
                if(player.buys > 0) {
                    board.filter { it.value > 0 && player.coins >= it.key.cost }.keys.toList() + listOf(null)
                } else {
                    listOf(null)
                }
            }
            MILITIA, CHAPEL -> player.hand
            WORKSHOP -> board.filter { it.key.cost < 5 && it.value > 0 }.keys.toList()

        }
    }

}