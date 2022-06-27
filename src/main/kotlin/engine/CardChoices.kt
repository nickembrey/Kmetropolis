package engine

typealias CardChoices = List<Card?>

fun getCardChoices(player: Player, board: Board, context: ChoiceContext): CardChoices {

    return when(context) {
        ChoiceContext.ACTION -> {
            if(player.actions > 0) {
                val actionCards = player.hand.filter { it.type == CardType.ACTION }.distinct()
                if(actionCards.isNotEmpty()) {
                    return actionCards + listOf(null)
                }
            }
            return listOf(null)
        }
        ChoiceContext.TREASURE -> player.hand.filter { it.type == CardType.TREASURE }.distinct() + listOf(null)
        ChoiceContext.BUY -> {
            if(player.buys > 0) {
                board.filter { it.value > 0 && player.coins >= it.key.cost }.keys.toList() + listOf(null)
            } else {
                listOf(null)
            }
        }
        ChoiceContext.MILITIA, ChoiceContext.CHAPEL -> player.hand
        ChoiceContext.WORKSHOP -> board.filter { it.key.cost < 5 && it.value > 0 }.keys.toList()

    }
}