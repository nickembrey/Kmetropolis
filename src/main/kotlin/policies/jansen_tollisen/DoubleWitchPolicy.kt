//package policies.jansen_tollisen
//
//import engine.*
//import engine.card.Card
//import engine.event.GameBranchOptions
//import engine.event.branch.GameBranchContext
//import policies.Policy
//import policies.PolicyName
//
//class DoubleWitchPolicy : Policy() {
//    override val name = PolicyName("singleWitchPolicy")
//    override fun finally() = Unit
//    override fun policy(
//        state: GameState,
//        choices: GameBranchOptions
//    ): Card? {
//        return when(state.gamePhase) {
//            GameBranchContext.CHOOSE_ACTION -> choices.firstNotNullOfOrNull { it }
//            GameChoicePhase.TREASURE -> choices.firstNotNullOfOrNull { it }
//            GameChoicePhase.BUY -> {
//                val goldCards: Int = state.currentPlayer.allCards.filter { it == Card.GOLD }.size
//                val witchCards = state.currentPlayer.allCards.filter { it == Card.WITCH }.size
//                val provinceCards = state.currentPlayer.allCards.filter { it == Card.PROVINCE }.size
//
//                val witchLeft = state.board[Card.WITCH]!!
//                val duchyLeft = state.board[Card.DUCHY]!!
//                val estateLeft = state.board[Card.ESTATE]!!
//
//
//                return if(state.currentPlayer.coins >= 8 && goldCards > 0) {
//                    Card.PROVINCE
//                } else if (state.currentPlayer.coins >= 5 && witchCards < 2 && witchLeft > 0) {
//                    Card.WITCH
//                } else if (state.currentPlayer.coins >= 5 && provinceCards < 4 && duchyLeft > 0) {
//                    Card.DUCHY
//                } else if (state.currentPlayer.coins >= 2 && provinceCards < 2 && estateLeft > 0) {
//                    Card.ESTATE
//                } else if (state.currentPlayer.coins >= 6) {
//                    Card.GOLD
//                } else if (state.currentPlayer.coins >= 3) {
//                    Card.SILVER
//                } else {
//                    null
//                }
//            }
//            GameChoicePhase.MILITIA -> choices.random()
//            else -> throw NotImplementedError()
//        }
//    }
//}
//
