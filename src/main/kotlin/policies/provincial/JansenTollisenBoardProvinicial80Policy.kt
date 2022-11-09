package policies.provincial

import engine.GameState
import engine.branch.*
import engine.card.Card
import policies.Policy
import policies.PolicyName
import policies.delegates.draw.RandomDrawPolicy
import policies.delegates.action.MPPAFPolicy

class JansenTollisenBoardProvinicial80Policy : Policy() {

    private val actionPolicy = MPPAFPolicy()
    private val drawPolicy = RandomDrawPolicy()

    override val name = PolicyName("jansenTollisenBoardProv80Policy")

    // TODO:
//    override fun endGame() {
//        buyMenu = mutableListOf(
//            Card.WITCH to 2,
//            Card.GOLD to 99,
//            Card.SILVER to 6,
//            Card.GARDENS to 8,
//            Card.SILVER to 99
//        )
//    }
    override fun finally() = Unit

    val duchyCondition: (GameState) -> Boolean = { it.board[Card.PROVINCE] <= 4 }
    val estateCondition: (GameState) -> Boolean = { it.board[Card.PROVINCE] <= 0 }

    private var buyMenu = mutableListOf(
        Card.WITCH to 2,
        Card.GOLD to 99,
        Card.LABORATORY to 1,
        Card.SILVER to 5,
        Card.GARDENS to 8,
        Card.SILVER to 99
    )

    override fun policy(
        state: GameState,
        branch: Branch
    ): BranchSelection {

        val options = branch.getOptions(state)

        return when (branch.context) {
            BranchContext.DRAW -> drawPolicy(state, branch)
            BranchContext.CHOOSE_ACTION -> actionPolicy(state, branch)
            BranchContext.CHOOSE_TREASURE -> options.firstOrNull { it is TreasureSelection } ?: options.first()
            BranchContext.CHOOSE_BUY -> {
                val coins = state.currentPlayer.coins
                if (coins >= 8) {
                    BuySelection(cards = listOf(Card.PROVINCE))
                } else if (duchyCondition(state) && coins >= 5 && state.board[Card.DUCHY] > 0) {
                    BuySelection(cards = listOf(Card.DUCHY))
                } else if (estateCondition(state) && coins >= 2 && state.board[Card.ESTATE] > 0) {
                    BuySelection(cards = listOf(Card.ESTATE))
                } else {
                    val entry = buyMenu.firstOrNull {
                        it.second > 0 && state.board[it.first] > 0 && coins >= it.first.cost
                    }
                    if (entry != null) {
                        val index = buyMenu.indexOfFirst { it.first == entry.first && it.second > 0 }
                        buyMenu[index] = (entry.first to entry.second - 1)
                        BuySelection(cards = listOf(entry.first))
                    } else {
                        SpecialBranchSelection.SKIP
                    }
                }
            }
            else -> throw NotImplementedError()
        }
    }
}