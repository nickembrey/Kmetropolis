package engine.branch

import com.github.shiguruikai.combinatoricskt.Combinatorics
import engine.GameEvent
import engine.GameState

data class Branch(val context: BranchContext, val selections: Int = 1): GameEvent {
    override val event = this

    fun getOptions(
        state: GameState,
        aggregated: Boolean = false
    ): Collection<BranchSelection> {

        return when (this.context) {
            BranchContext.DRAW -> {
                if(aggregated) {
                    val deck = state.currentPlayer.deck
                    when(deck.size) {
                        0 -> BranchContext.skipList
                        selections -> listOf(DrawSelection(cards = deck, probability = 1.0))
                        in 1 until selections -> state.currentPlayer
                            .getDiscardCombinations(selections - deck.size)
                            .map { DrawSelection(cards = deck.plus(it.key), probability = it.value ) }
                        else -> state.currentPlayer.getDrawCombinations(selections)
                            .map { DrawSelection(cards = it.key, probability = it.value )}
                    }
                } else {
                    when(state.currentPlayer.deck.size) {
                        0 -> BranchContext.skipList
                        else -> state.currentPlayer.getDrawProbabilities().map {
                            DrawSelection(cards = listOf(it.key), probability = it.value)
                        }
                    }
                }
            }
            BranchContext.CHOOSE_BUYS -> {
                if(aggregated) {
                    return Combinatorics.combinationsWithRepetition(state.buyMenu, state.currentPlayer.buys)
                        .map { BuySelection(cards = it) }
                        .filter { it.cards.sumOf { card -> card.cost } <= state.currentPlayer.coins }.toList()
                        .ifEmpty { listOf(SpecialBranchSelection.SKIP) }
                } else {
                    state.buyMenu.map { BuySelection( cards = listOf(it) ) }.plus(SpecialBranchSelection.SKIP)
                }
            }
            BranchContext.GAME_OVER -> BranchContext.gameOver
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