package engine.branch

import com.github.shiguruikai.combinatoricskt.Combinatorics
import com.github.shiguruikai.combinatoricskt.combinations
import engine.GameEvent
import engine.GameState
import engine.card.Card
import engine.card.CardType
import util.input.buildCardSelection

// TODO: "selections" is a confusing name
data class Branch(val context: BranchContext, val selections: Int = 1): GameEvent {

    companion object {
        val gameOver = listOf(SpecialBranchSelection.GAME_OVER)
        val skipList = listOf(SpecialBranchSelection.SKIP)
    }

    fun getOptions(
        state: GameState
    ): Collection<BranchSelection> {

        if(!state.currentPlayer.policy.hidden) {
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
                ).map { VisibleCellarSelection(it) }.toList()
                BranchContext.DRAW -> listOf(VisibleDrawSelection(cards = state.currentPlayer.sample(selections)))
                // TODO: make sure skiplist exists where applicable
                BranchContext.CHOOSE_BUY -> return Combinatorics.combinationsWithRepetition(state.buyMenu, state.currentPlayer.buys)
                    .filter { it.sumOf { card -> card.cost } <= state.currentPlayer.coins }
                    .filter { state.board >= it }
                    .map { BuySelection(cards = it) }
                    .plus(SpecialBranchSelection.SKIP).shuffled().toList()
                BranchContext.GAME_OVER -> gameOver
                BranchContext.CHAPEL -> hand.combinations(selections).toList()
                    .map { ChapelSelection(cards = it) }
                    .ifEmpty { skipList }
                BranchContext.HARBINGER -> {
                    // TODO: check that discard is all visible
                    state.currentPlayer.knownDiscard.possibilities
                        .map { HarbingerSelection(card = it) }
                        .plus(HarbingerSelection(card = null))
                }
                BranchContext.VASSAL_DISCARD -> if(state.currentPlayer.knownDeck[0] != null) {
                    listOf(VassalDiscardSelection(card = state.currentPlayer.knownDeck[0]!!))
                } else {
                    val sampleList = state.currentPlayer.sample(1)
                    if(sampleList.isEmpty()) {
                        return skipList
                    } else {
                        listOf(VassalDiscardSelection(card = sampleList.single()))
                    }
                }
                BranchContext.VASSAL_PLAY -> listOf(SpecialBranchSelection.SKIP, VassalPlaySelection(card = state.currentPlayer.vassalCard!!))
                BranchContext.WORKSHOP -> state.workshopMenu.map { WorkshopSelection(card = it) }
                BranchContext.BUREAUCRAT -> hand.filter { it.type == CardType.VICTORY }.map {
                    BureaucratSelection(card = it)
                }.ifEmpty { skipList }
                BranchContext.MILITIA -> hand.combinations(selections).distinct().toList().map { VisibleMilitiaSelection(cards = it) }
                BranchContext.POACHER -> hand
                    .map { VisiblePoacherSelection(card = it) }
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
                BranchContext.BANDIT -> {
                    listOf(state.currentPlayer.knownDeck[0]!!, state.currentPlayer.knownDeck[1]!!)
                        .filter { it == Card.SILVER || it == Card.GOLD }
                        .map { BanditSelection(card = it) }
                        .ifEmpty { skipList }
                }
                BranchContext.LIBRARY_IDENTIFY -> {
                    val topCard: Card = if (state.currentPlayer.knownDeck[0] != null) {
                        state.currentPlayer.knownDeck[0]!!
                    } else {
                        state.currentPlayer.sample(1).single()
                    }
                    return listOf(VisibleLibraryIdentifySelection(card = topCard))
                }
                BranchContext.LIBRARY_DRAW -> {
                    val card = state.currentPlayer.knownDeck[0]!!
                    if(card.type == CardType.ACTION) {
                        return listOf(VisibleLibrarySkipSelection(card), VisibleLibraryDrawSelection(card))
                    } else {
                        return listOf(VisibleLibraryDrawSelection(card))
                    }
                }
                BranchContext.MINE_TRASH -> hand
                    .filter { it.type == CardType.TREASURE }
                    .map { MineTrashSelection(card = it) }
                    .ifEmpty { skipList }
                BranchContext.MINE_GAIN -> state.board.possibilities
                    .filter { it.cost <= (state.currentPlayer.mineCard!!.cost + 3 ) }
                    .map { MineGainSelection(card = it) }
                    .ifEmpty { skipList }
                BranchContext.SENTRY_IDENTIFY -> {
                    val samples = state.currentPlayer.sample(2)
                    val cards: MutableList<Card> = mutableListOf()
                    for(i in samples.indices) {
                        if(state.currentPlayer.knownDeck[i] == null) {
                            cards.add(samples[i])
                        } else {
                            cards.add(state.currentPlayer.knownDeck[i]!!)
                        }
                    }
                    return listOf(SentryIdentifySelection(cards = cards.withIndex().map { (i, card) -> Pair(card, i)}))
                }
                BranchContext.SENTRY_TRASH -> listOf( // TODO: what about the case where there aren't 2 cards on deck
                    listOf(Pair(state.currentPlayer.knownDeck[0]!!, 0), Pair(state.currentPlayer.knownDeck[1]!!, 1)),
                    listOf(Pair(state.currentPlayer.knownDeck[0]!!, 0)),
                    listOf(Pair(state.currentPlayer.knownDeck[1]!!, 1)),
                    listOf()
                ).map { SentryTrashSelection(cards = it) }
                BranchContext.SENTRY_DISCARD -> {
                    val cardList: MutableList<Pair<Card, Int>> = mutableListOf()
                    if(state.currentPlayer.knownDeck[0] != null) {
                        cardList.add(Pair(state.currentPlayer.knownDeck[0]!!, 0))
                    }
                    if(state.currentPlayer.knownDeck[1] != null) {
                        cardList.add(Pair(state.currentPlayer.knownDeck[1]!!, 1))
                    }
                    when(cardList.size) {
                        0 -> skipList
                        1 -> listOf(SentryDiscardSelection(cards = cardList), SpecialBranchSelection.SKIP)
                        2 -> listOf(
                            listOf(cardList[0]),
                            listOf(cardList[1]),
                            listOf(cardList[0], cardList[1]))
                            .map { SentryDiscardSelection(cards = it) }
                            .plus(SpecialBranchSelection.SKIP)
                        else -> throw IllegalStateException()
                    }
                }
                BranchContext.SENTRY_TOPDECK -> {
                    val cardList: MutableList<Pair<Card, Int>> = mutableListOf()
                    if(state.currentPlayer.knownDeck[0] != null) {
                        cardList.add(Pair(state.currentPlayer.knownDeck[0]!!, 0))
                    }
                    if(state.currentPlayer.knownDeck[1] != null) {
                        cardList.add(Pair(state.currentPlayer.knownDeck[1]!!, 1))
                    }
                    when(cardList.size) {
                        0 -> skipList
                        1 -> listOf(SentryTopdeckSelection(cards = cardList))
                        2 -> listOf(
                            listOf(cardList[1], cardList[0]),
                            listOf(cardList[0], cardList[1]))
                            .map { SentryTopdeckSelection(cards = it) }
                        else -> throw IllegalStateException()
                    }
                }
                BranchContext.ARTISAN_GAIN -> state.board.possibilities
                    .filter { it.cost <= 5 }
                    .map { ArtisanGainSelection(card = it) }
                BranchContext.ARTISAN_TOPDECK -> hand
                    .map { ArtisanTopdeckSelection(card = it) }
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
        } else {

            println("Player: ${state.currentPlayer.playerNumber} (${state.currentPlayer.name})")
            println("Context: $context")
            println("Selections: $selections")
            println("")

            return when(context) {
                BranchContext.ATTACK -> listOf(AttackSelection(block = true), AttackSelection(block = false))
                BranchContext.GAME_OVER -> gameOver
                BranchContext.DRAW -> listOf(HiddenDrawSelection(cardCount = selections))
                BranchContext.CHOOSE_ACTION -> state.board.possibilities
                    .filter { it.type == CardType.ACTION }
                    .map { ActionSelection(card = it) }
                    .plus(SpecialBranchSelection.SKIP)
                BranchContext.CHOOSE_TREASURE -> listOf(TreasureSelection(cards = buildCardSelection(state, this)))
                BranchContext.CHOOSE_BUY -> listOf(BuySelection(cards = buildCardSelection(state, this)))
                BranchContext.CELLAR -> listOf(HiddenCellarSelection(cardCount = selections))
                BranchContext.CHAPEL -> listOf(ChapelSelection(cards = buildCardSelection(state, this)))
                BranchContext.HARBINGER -> state.board.possibilities
                    .map { HarbingerSelection(card = it) }
                    .plus(SpecialBranchSelection.SKIP)
                BranchContext.VASSAL_DISCARD -> if(state.currentPlayer.knownDeck[0] != null) {
                    listOf(VassalDiscardSelection(card = state.currentPlayer.knownDeck[0]!!))
                } else {
                    state.board.possibilities
                        .map { VassalDiscardSelection(card = it) }
                        .plus(SpecialBranchSelection.SKIP)
                }
                BranchContext.VASSAL_PLAY -> state.board.possibilities
                    .filter { it.type == CardType.ACTION }
                    .map { VassalPlaySelection(card = it) }
                    .plus(SpecialBranchSelection.SKIP)
                BranchContext.WORKSHOP -> state.board.possibilities
                    .map { WorkshopSelection(card = it) }
                    .plus(SpecialBranchSelection.SKIP)
                BranchContext.BUREAUCRAT -> state.board.possibilities
                    .map { BureaucratSelection(card = it) }
                    .plus(SpecialBranchSelection.SKIP)
                BranchContext.MILITIA -> listOf(HiddenMilitiaSelection(cardCount = selections))
                BranchContext.POACHER -> listOf(HiddenPoacherSelection())
                BranchContext.REMODEL_TRASH -> state.board.possibilities
                    .map { RemodelTrashSelection(card = it) }
                    .plus(SpecialBranchSelection.SKIP)
                BranchContext.REMODEL_GAIN -> state.board.possibilities
                    .map { RemodelGainSelection(card = it) }
                    .plus(SpecialBranchSelection.SKIP)
                BranchContext.THRONE_ROOM -> state.board.possibilities
                    .map { ThroneRoomSelection(card = it) }
                    .plus(SpecialBranchSelection.SKIP)
                BranchContext.BANDIT -> state.board.possibilities
                    .map { BanditSelection(card = it) }
                    .plus(SpecialBranchSelection.SKIP)
                BranchContext.LIBRARY_IDENTIFY -> listOf(HiddenLibraryIdentifySelection())
                BranchContext.LIBRARY_DRAW -> listOf(HiddenLibraryDrawSelection(), HiddenLibrarySkipSelection())
                BranchContext.MINE_TRASH -> state.board.possibilities
                    .map { MineTrashSelection(card = it) }
                    .plus(SpecialBranchSelection.SKIP)
                BranchContext.MINE_GAIN -> state.board.possibilities
                    .map { MineGainSelection(card = it) }
                    .plus(SpecialBranchSelection.SKIP)
                BranchContext.SENTRY_IDENTIFY -> listOf(SentryIdentifySelection(
                    cards = buildCardSelection(state, this)
                        .mapIndexed { i, c -> Pair(c, i) }
                ))
                BranchContext.SENTRY_TRASH -> listOf(SentryTrashSelection(
                    cards = buildCardSelection(state, this)
                        .mapIndexed { i, c -> Pair(c, i) }
                ))
                BranchContext.SENTRY_DISCARD -> listOf(SentryDiscardSelection(
                    cards = buildCardSelection(state, this)
                        .mapIndexed { i, c -> Pair(c, i) }
                ))
                BranchContext.SENTRY_TOPDECK -> listOf(SentryTopdeckSelection(
                    cards = buildCardSelection(state, this)
                        .mapIndexed { i, c -> Pair(c, i) }
                ))
                BranchContext.ARTISAN_GAIN -> state.board.possibilities
                    .map { ArtisanGainSelection(card = it) }
                BranchContext.ARTISAN_TOPDECK -> state.board.possibilities
                    .map { ArtisanTopdeckSelection(card = it) }
                BranchContext.ANY, BranchContext.NONE -> throw IllegalStateException()
            }
        }
    }
}