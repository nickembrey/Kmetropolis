package engine.branch

import com.github.shiguruikai.combinatoricskt.Combinatorics
import engine.GameEvent
import engine.GameState
import engine.card.Card
import engine.card.CardType

// TODO: "selections" is a confusing name
data class Branch(val context: BranchContext, val selections: Int = 1): GameEvent {

    companion object {
        val gameOver = listOf(SpecialBranchSelection.GAME_OVER)
        val skipList = listOf(SpecialBranchSelection.SKIP)
    }

    fun getOptions(state: GameState): Collection<BranchSelection> {

        if(!state.currentPlayer.visibleHand) {
            throw IllegalStateException()
        }

        val hand = state.currentPlayer.knownHand.toList()

        return when (this.context) {

            BranchContext.ATTACK -> if(hand.contains(Card.MOAT)) {
                listOf(AttackSelection(block = true), AttackSelection(block = false))
            } else {
                listOf(AttackSelection(block = false))
            }

            // TODO: make sure cellar is no longer in hand when this gets activated
            BranchContext.CELLAR -> Combinatorics.combinations(
                hand,
                hand.size
            ).map { CellarSelection(it) }.toList()
            BranchContext.DRAW -> listOf(DrawSelection(cards = state.currentPlayer.sample(selections)))
            // TODO: make sure skiplist exists where applicable
            BranchContext.CHOOSE_BUY -> return Combinatorics.combinationsWithRepetition(state.buyMenu, state.currentPlayer.buys)
                .filter { it.sumOf { card -> card.cost } <= state.currentPlayer.coins }
                .filter { state.board >= it }
                .map { BuySelection(cards = it) }
                .plus(SpecialBranchSelection.SKIP).shuffled().toList()
            BranchContext.GAME_OVER -> gameOver
            BranchContext.CHAPEL -> hand.map { ChapelSelection(card = it) }
            BranchContext.HARBINGER -> {
                // TODO: check that discard is all visible
                state.currentPlayer.knownDiscard.possibilities.map { HarbingerSelection(card = it) }
            }
            BranchContext.VASSAL -> listOf(SpecialBranchSelection.SKIP, VassalSelection(card = state.currentPlayer.vassalCard!!))
            BranchContext.WORKSHOP -> state.workshopMenu.map { WorkshopSelection(card = it) }
            BranchContext.BUREAUCRAT -> hand.filter { it.type == CardType.VICTORY }.map {
                BureaucratSelection(card = it)
            }.ifEmpty { skipList }
            BranchContext.MILITIA -> hand.map { MilitiaSelection(card = it) }
            BranchContext.REMODEL_TRASH -> hand
                .map { RemodelTrashSelection(card = it) }.ifEmpty { skipList }
            BranchContext.REMODEL_GAIN -> if(state.currentPlayer.remodelCard == null) {
                skipList
            } else {
                state.remodelMenu.map { RemodelGainSelection(card = it) }.ifEmpty { skipList }
            }
            BranchContext.THRONE_ROOM -> hand
                .filter { it.type == CardType.ACTION }
                .map { ThroneRoomSelection(card = it) }
                .ifEmpty { skipList }
            BranchContext.CHOOSE_ACTION -> hand
                .filter { it.type == CardType.ACTION }
                .map { ActionSelection(card = it) }
                .ifEmpty { skipList }
            BranchContext.CHOOSE_TREASURE -> {
                val treasures = hand.filter { it.type == CardType.TREASURE }.toList()
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