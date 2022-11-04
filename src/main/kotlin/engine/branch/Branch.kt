package engine.branch

import com.github.shiguruikai.combinatoricskt.Combinatorics
import engine.GameEvent
import engine.GameState
import engine.card.CardLocation
import engine.card.CardType
import engine.operation.state.player.PlayerMoveCardsOperation

data class Branch(val context: BranchContext, val selections: Int = 1): GameEvent {

    companion object {
        val gameOver = listOf(SpecialBranchSelection.GAME_OVER)
        val skipList = listOf(SpecialBranchSelection.SKIP)
    }

    fun getOptions(
        state: GameState,
        aggregated: Boolean = false
    ): Collection<BranchSelection> {

        return when (this.context) {
            // TODO: make sure cellar is no longer in hand when this gets activated
            BranchContext.CELLAR -> Combinatorics.combinations(
                state.currentPlayer.hand,
                state.currentPlayer.hand.size
            ).map {
                CellarSelection(it)
            }.toList()
            BranchContext.DRAW -> {
                if(aggregated) {
                    val deck = state.currentPlayer.deck
                    when(deck.size) {
                        0 -> {
                            // TODO:
                            if(state.currentPlayer.discard.isNotEmpty()) {
                                state.operationHistory.add(
                                    PlayerMoveCardsOperation(
                                        cards = state.currentPlayer.discard.toMutableList(),
                                        from = CardLocation.DISCARD,
                                        to = CardLocation.DECK
                                    )
                                )
                                state.currentPlayer.shuffle()
                                return getOptions(state, true)
                            } else {
                                skipList
                            }
                        }
                        selections -> listOf(DrawSelection(cards = deck, probability = 1.0))
                        in 1 until selections -> state.currentPlayer// TODO:
                            .getDiscardCombinations(selections - deck.size)
                            .map { DrawSelection(cards = deck.plus(it.key), probability = it.value ) }
                        else -> {
                            state.currentPlayer.getDrawCombinations(selections)
                                .map { DrawSelection(cards = it.key, probability = it.value )}
                        }
                    }
                } else {
                    // TODO: always aggregated, since I don't think this adds anything
                    when(state.currentPlayer.deck.size) {
                        0 -> skipList
                        else -> state.currentPlayer.getDrawProbabilities().map {
                            DrawSelection(cards = listOf(it.key), probability = it.value)
                        }.also {
                            if(it[0].cards.size != 1) {
                                throw IllegalStateException()
                            }
                        }
                    }
                }
            } // TODO: need to shuffle them all, not just buy?
            // TODO: make sure skiplist exists where applicable
            BranchContext.CHOOSE_BUY -> return Combinatorics.combinationsWithRepetition(state.buyMenu, state.currentPlayer.buys)
                .map { BuySelection(cards = it) }
                .filter { it.cards.sumOf { card -> card.cost } <= state.currentPlayer.coins }
                .plus(SpecialBranchSelection.SKIP).shuffled().toList()
            BranchContext.GAME_OVER -> gameOver
            BranchContext.CHAPEL -> state.currentPlayer.hand.map { ChapelSelection(card = it) }
            BranchContext.WORKSHOP -> state.workshopMenu.map { WorkshopSelection(card = it) }
            BranchContext.MILITIA -> state.currentPlayer.hand.map { MilitiaSelection(card = it) }
            BranchContext.REMODEL_TRASH -> state.currentPlayer.hand
                .map { RemodelTrashSelection(card = it) }.ifEmpty { skipList }
            BranchContext.REMODEL_GAIN -> if(state.currentPlayer.remodelCard == null) {
                skipList
            } else {
                state.remodelMenu.map { RemodelGainSelection(card = it) }.ifEmpty { skipList }
            }
            BranchContext.CHOOSE_ACTION -> state.currentPlayer.actionMenu
            BranchContext.CHOOSE_TREASURE -> {
                val treasures = state.currentPlayer.hand.filter { it.type == CardType.TREASURE }.toList()
                if(treasures.isNotEmpty()) {
                    listOf(TreasureSelection(cards = treasures)) // state.currentPlayer.treasureMenu
                } else {
                    skipList
                }
            }
            BranchContext.ANY, BranchContext.NONE -> throw IllegalStateException()
        }
    }
}