package engine.branch

import engine.GameEvent
import engine.GameState
import engine.card.Card
import engine.card.CardLocation
import engine.operation.state.player.PlayerMoveCardsOperation

data class Branch(val context: BranchContext, val selections: Int = 1): GameEvent {
    override val event = this

    fun getOptions(
        state: GameState,
        groupDraws: Boolean = false
    ): Collection<BranchSelection> {

        return when (this.context) {
            BranchContext.DRAW -> when(state.currentPlayer.deckSize) {
                0 -> BranchContext.skipList
                else -> {
                    if(groupDraws) {
                        val deckList = state.currentPlayer.deck

                        if(deckList.size == selections) {
                            listOf(DrawSelection(cards = deckList.toMutableList(), probability = 1.0))
                        } else if(deckList.size < selections) {
                            // TODO: get rid of pruning in GameState after this is done
                            val base = state.currentPlayer
                                .getDiscardCombinations(selections - deckList.size)
                                .map { DrawSelection(cards = deckList.plus(it.key), probability = it.value ) }
//                            base.filter { it.probability > 0.1 }.ifEmpty {
//                                base
//                            }
                            base
                        } else {
                            val base = state.currentPlayer.getDrawCombinations(selections)
                                .map { DrawSelection(cards = it.key, probability = it.value )}
//                            base.filter { it.probability > 0.1 }.ifEmpty {
//                                base
//                            }
                            base
                        }
                    } else {
                        val base = state.currentPlayer.getDrawProbabilities().map {
                            DrawSelection(cards = listOf(it.key), probability = it.value)
                        }
//                        base.filter { it.probability > 0.1 }.ifEmpty {
//                            base
//                        }
                        base
                    }
                }
            }
            BranchContext.GAME_OVER -> BranchContext.gameOver
            BranchContext.CHOOSE_BUYS -> state.buyMenu
            BranchContext.CHAPEL -> state.currentPlayer.hand
            BranchContext.WORKSHOP -> state.workshopMenu
            BranchContext.MILITIA -> state.currentPlayer.hand
            BranchContext.REMODEL_TRASH -> state.currentPlayer.hand.ifEmpty { BranchContext.skipList }
            BranchContext.REMODEL_GAIN -> state.remodelMenu.ifEmpty { BranchContext.skipList }
            BranchContext.CHOOSE_ACTION -> state.currentPlayer.actionMenu
            BranchContext.CHOOSE_TREASURE -> state.currentPlayer.treasureMenu
            BranchContext.ANY, BranchContext.NONE -> throw IllegalStateException()
        }
    }
}