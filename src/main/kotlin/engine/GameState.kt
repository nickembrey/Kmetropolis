package engine

import engine.branch.*
import engine.card.*
import engine.operation.GameOperation
import engine.operation.PlayerOperation
import engine.operation.state.player.PlayerSimpleOperation
import engine.operation.Operation
import engine.operation.property.PropertyOperation
import engine.operation.stack.*
import engine.operation.state.game.GameSimpleOperation
import engine.operation.property.*
import engine.operation.stack.game.GameCompoundOperation
import engine.operation.stack.player.PlayerCardOperation
import engine.operation.stack.player.PlayerCardOperation.Companion.PLAY_FROM_DISCARD
import engine.operation.stack.player.PlayerCardOperationType
import engine.operation.state.StateOperation
import engine.operation.state.game.GameCardOperation
import engine.operation.state.game.GameCardOperationType
import engine.operation.state.player.PlayerMoveCardOperation
import engine.operation.state.player.PlayerMoveCardsOperation
import engine.performance.util.CardCountMap
import engine.player.Player
import engine.player.PlayerNumber
import engine.player.PlayerProperty
import policies.Policy
import policies.PolicyName
import java.util.*

// TODO: take out everything unnecessary and make it injectable so that the state can be left on the tree

fun List<Player>.copy(policies: List<Policy>) =
    mapIndexed { index, player ->
        player.copyPlayer(policies[index])
    }

// TODO: if we always use the currentPlayer, can we just have state implement Player?
class GameState private constructor (
    val players: List<Player>, // needs to be in order // TODO: replace with two args
    val board: CardCountMap,
    var currentPlayerNumber: PlayerNumber,
    var phase: GamePhase, // TODO: remove
    var eventStack: EventStack,
    var turns: Int,
    val maxTurns: Int,
    var emptyPiles: Int,
    val branchSelectionHistory: MutableList<Triple<PolicyName, BranchContext, BranchSelection>>, // TODO: special data class for this
    private val log: Boolean
) {

    private val playerOne = players[0]
    private val playerTwo = players[1]

    companion object {
        fun new(
            policy1: Policy,
            policy2: Policy,
            board: CardCountMap,
            maxTurns: Int,
            log: Boolean,
            startingPolicy: Int? = null
        ): GameState = GameState(
                players = when(startingPolicy) {
                    null -> listOf(policy1, policy2).shuffled()
                    1 -> listOf(policy1, policy2)
                    2 -> listOf(policy2, policy1)
                    else -> throw IllegalStateException()
                }.mapIndexed { i, policy -> Player.new(board, PlayerNumber.fromInt(i), policy) },
                currentPlayerNumber = PlayerNumber.PLAYER_ONE,
                board = board,
                phase = GamePhase.NOT_STARTED,
                eventStack = EventStack(mutableListOf(SpecialGameEvent.START_GAME)), // TODO: new constructor
                turns = 0,
                maxTurns = maxTurns,
                emptyPiles = 0,
                branchSelectionHistory = ArrayList(500), // TODO: audit capacities
                log = log
            )
    }

    fun copy(
        newPolicies: List<Policy> = players.map { it.policy },
        newEventStack: EventStack = eventStack.copy(),
        newMaxTurns: Int = maxTurns,
        keepHistory: Boolean = false,
        newLog: Boolean = log
    ): GameState {

        return GameState(
            players = players.copy(newPolicies),
            currentPlayerNumber = currentPlayerNumber,
            board = board.copy(),
            phase = phase,
            eventStack = newEventStack,
            turns = turns,
            maxTurns = newMaxTurns,
            emptyPiles = emptyPiles,
            branchSelectionHistory = if(keepHistory) {
                ArrayList(branchSelectionHistory).apply { ensureCapacity(1200) }
            } else {
                ArrayList(1200) // TODO: don't need any capacity if not using...?
            },
            log = newLog
        )
    }

    val currentPlayer: Player
        get() = when(currentPlayerNumber) {
            PlayerNumber.PLAYER_ONE -> playerOne
            PlayerNumber.PLAYER_TWO -> playerTwo
        }
    val otherPlayer: Player
        get() = when(currentPlayer.playerNumber) {
            PlayerNumber.PLAYER_ONE -> playerTwo
            PlayerNumber.PLAYER_TWO -> playerOne
        }

    // TODO: list of BuySelection
    // TODO: seems like we're getting curses even when they're gone?
    private val buyMenus: List<ArrayList<Card>> = (0..8)
        .map { coins -> ArrayList(
            board.toMap().filter { it.key.cost <= coins && it.value > 0 }.keys
        )}
    val buyMenu: ArrayList<Card>
        get() = when(currentPlayer.coins) {
        in 0..7 -> buyMenus[currentPlayer.coins]
        else -> buyMenus[8] // TODO:
    }
    val workshopMenu: ArrayList<Card>
        get() = buyMenus[4]
    val remodelMenu: ArrayList<Card>
        get() = when(currentPlayer.remodelCard!!.cost) {
            in 0..5 -> buyMenus[currentPlayer.remodelCard!!.cost + 2]
            else -> buyMenus[8]
        }

    var gameOver = false

    fun opponentRedraw() {

        otherPlayer.redeck()

        eventStack.pushAll( listOf(
            SpecialGameEvent.SWITCH_PLAYER,
            Branch(BranchContext.DRAW, otherPlayer.handCount),
            SpecialGameEvent.SWITCH_PLAYER
        ))
    }

    fun getNextEvent(): GameEvent = eventStack.pop()

    fun getNextBranch(): Branch =
        when(val event = eventStack.pop()) {
            is Branch -> event
            SpecialGameEvent.END_GAME -> {
                gameOver = true
                Branch(context = BranchContext.GAME_OVER)
            }
            else -> {
                processEvent(event)
                getNextBranch()
            }
        }


    // TODO: this should be parcelled out between stack and state
    private fun processPropertyOperation(operation: PropertyOperation) {

        when(operation) {
            is ModifyPropertyOperation -> when(operation.target) {

                // TODO: DRY
                PlayerProperty.BUYS -> {
                    val old = currentPlayer.buys
                    currentPlayer.buys += operation.modification
                }
                PlayerProperty.COINS -> {
                    val old = currentPlayer.coins
                    currentPlayer.coins += operation.modification
                }
                PlayerProperty.BASE_VP -> {
                    val old = currentPlayer.baseVp
                    currentPlayer.baseVp += operation.modification
                }
                else -> throw NotImplementedError()
            }.also {
                if(operation.modification == 0) {
                    throw IllegalStateException("Modifications should never be 0")
                }
            }
            is ReadPropertyOperation<*> -> throw NotImplementedError()
            is SetFromPropertyOperation<*> -> { // TODO: check up on this?

                when(operation.target) {
                    is GameProperty -> when(operation.target) {
                        // TODO: does this mean maybe player shouldn't be a property?
                        GameProperty.PLAYER -> throw IllegalArgumentException("Changing player can be handled with a simple operation!")
                        GameProperty.PHASE -> phase = operation.to as GamePhase // TODO:
                        // TODO: does this mean maybe turns shouldn't be a property?
                        GameProperty.TURNS -> throw IllegalArgumentException("Incrementing turns can be handled with a simple operation!")
                        // TODO: does this mean maybe game over shouldn't be a property?
                        GameProperty.GAME_OVER -> throw IllegalArgumentException("Setting game over can be handled with a simple operation!")
                    }
                    is PlayerProperty -> when(operation.target) {
                        // TODO: casting?
                        PlayerProperty.BUYS -> currentPlayer.buys = operation.to as Int
                        PlayerProperty.COINS -> currentPlayer.coins = operation.to as Int
                        PlayerProperty.BASE_VP -> currentPlayer.baseVp = operation.to as Int
                        PlayerProperty.REMODEL_CARD -> currentPlayer.remodelCard = operation.to as Card?
                    }
                    else -> throw IllegalArgumentException()
                }

            } // TODO: need to put SetProperty under StateOperations
            is SetToPropertyOperation<*> -> when(operation.target) {
                PlayerProperty.BUYS -> {
                    val old = currentPlayer.buys
                    currentPlayer.buys = operation.to as Int // TODO
                }
                PlayerProperty.COINS -> {
                    val old = currentPlayer.coins
                    currentPlayer.coins = operation.to as Int // TODO
                }
                PlayerProperty.BASE_VP -> {
                    val old = currentPlayer.baseVp
                    currentPlayer.baseVp = operation.to as Int // TODO
                }
                PlayerProperty.REMODEL_CARD -> {
                    val old = currentPlayer.remodelCard
                    currentPlayer.remodelCard = operation.to as Card // TODO
                }
                else -> throw NotImplementedError()
            }
        }
    }

    private fun processStackOperation(operation: StackOperation) {
        when(operation) {
            is GameOperation -> when (operation) { // TODO:
                is GameCompoundOperation -> when(operation) { // TODO:
                    GameCompoundOperation.NEXT_PHASE -> processPropertyOperation(SetFromPropertyOperation.NEXT_PHASE(phase)) // TODO:
                }
                else -> throw IllegalArgumentException()
            }
            is PlayerOperation -> when(operation) {
                is PlayerCardOperation -> when(operation.type) {
                    PlayerCardOperationType.DRAW -> {
                        processStateOperation(
                            PlayerMoveCardOperation.MOVE_CARD(operation.card, CardLocation.DECK, CardLocation.HAND)
                        )
                    }
                    PlayerCardOperationType.PLAY, PlayerCardOperationType.PLAY_FROM_DISCARD, PlayerCardOperationType.PLAY_WITH_THRONE -> {

                        when(operation.card.type) {
                            CardType.ACTION, CardType.TREASURE -> {
                                eventStack.pushAll(
                                    List(operation.card.addActions) { Branch(BranchContext.CHOOSE_ACTION) }
                                )
                                eventStack.push(
                                    DelayedGameOperation(operation = {
                                        operation.card.effect(it)
                                    })
                                )
                                if(operation.card.addCards > 0) {
                                    eventStack.push(
                                        Branch(BranchContext.DRAW, options = operation.card.addCards)
                                    )
                                }
                                if(operation.card.addCoins > 0) {
                                    processOperation(ModifyPropertyOperation.MODIFY_COINS(operation.card.addCoins))
                                }
                                if(operation.card.addBuys > 0) {
                                    processOperation(ModifyPropertyOperation.MODIFY_BUYS(operation.card.addBuys))
                                }
                                when(operation.type) {
                                    PlayerCardOperationType.PLAY -> currentPlayer.play(operation.card)
                                    PlayerCardOperationType.PLAY_WITH_THRONE -> {}
                                    PlayerCardOperationType.PLAY_FROM_DISCARD -> currentPlayer.playFromDiscard(operation.card)
                                    else -> throw IllegalStateException()
                                }

                            }
                            else -> throw IllegalStateException()
                        }
                    }
                    PlayerCardOperationType.BUY -> {
                        processOperation(PlayerCardOperation.GAIN(operation.card))
                        if(operation.card.cost > 0) {
                            processOperation(ModifyPropertyOperation.MODIFY_COINS(-operation.card.cost))
                        }
                    }
                    PlayerCardOperationType.GAIN -> {
                        processOperation(GameCardOperation.DECREMENT_SUPPLY(operation.card))
                        if(operation.card.vp != 0) {
                            processOperation(ModifyPropertyOperation.MODIFY_VP(operation.card.vp))
                        }
                        currentPlayer.gain(operation.card)
                    }
                    PlayerCardOperationType.TRASH -> {
                        if(operation.card.vp != 0) {
                            processOperation(ModifyPropertyOperation.MODIFY_VP(-operation.card.vp))
                        }
                        currentPlayer.trash(operation.card)
                    }
                }
                else -> throw NotImplementedError()
            }
            is StackConditionalOperation -> {
                if(operation.condition(this)) {
                    if(operation.loopOnTrue) {
                        processOperation(operation)
                    }
                    when (operation.conditionalEvent) {
                        is Branch -> eventStack.push(operation.conditionalEvent)
                        is Operation -> processOperation(operation.conditionalEvent)
                        else -> throw IllegalStateException()
                    }
                }
            }
            is StackMultipleOperation -> {
                for(event in operation.events) {
                    when (event) {
                        is Branch -> eventStack.push(event)
                        is Operation -> processOperation(event)
                        else -> throw IllegalStateException()
                    }
                }
            }
            is StackRepeatedOperation -> {
                for(i in 1..operation.repeatFn(this)) { // TODO: make sure this is end-inclusive
                    when (operation.repeatedEvent) {
                        is Branch -> eventStack.push(operation.repeatedEvent)
                        is Operation -> processOperation(operation.repeatedEvent)
                        else -> throw IllegalStateException()
                    }
                }
            }
            is StackSimpleOperation -> when(operation) {
                StackSimpleOperation.SKIP_CONTEXT -> {} // do nothing
            }
        }
    }

    private fun processStateOperation(operation: StateOperation) {

        when(operation) {

            // TODO: is GameOperation

            is GameCardOperation -> when(operation.type) {
                GameCardOperationType.DECREMENT_CARD_SUPPLY -> {
                    val numberLeft = board[operation.card] - 1 // TODO:
                    board[operation.card] = numberLeft
                    if(numberLeft == 0) {
                        emptyPiles += 1
                        buyMenus.forEach { it.remove(operation.card) } // TODO:
                    }
                }
                GameCardOperationType.INCREMENT_CARD_SUPPLY -> {
                    val previousNumber = board[operation.card]
                    board[operation.card] = previousNumber + 1
                    if(previousNumber == 0) {
                        emptyPiles -= 1
                        buyMenus.filterIndexed { index, _ -> operation.card.cost <= index }
                            .forEach { it.add(operation.card) } // TODO:
                    }
                }
            }

            is GameSimpleOperation -> when(operation) {
                GameSimpleOperation.INCREMENT_TURNS -> turns += 1
                GameSimpleOperation.DECREMENT_TURNS -> turns -= 1
                GameSimpleOperation.SWITCH_PLAYER -> currentPlayerNumber = currentPlayerNumber.next
                GameSimpleOperation.INCREMENT_EMPTY_PILES -> emptyPiles += 1
                GameSimpleOperation.DECREMENT_EMPTY_PILES -> emptyPiles -= 1
                GameSimpleOperation.END_GAME -> gameOver = true
                GameSimpleOperation.UNDO_END_GAME -> gameOver = false
            } // TODO: log on debug

            is PlayerOperation -> when(operation) {
                is PlayerMoveCardOperation -> throw IllegalStateException()
                is PlayerMoveCardsOperation -> throw IllegalStateException()
                is PlayerSimpleOperation -> when(operation) {
                    PlayerSimpleOperation.INCREMENT_BUYS -> currentPlayer.buys += 1
                    PlayerSimpleOperation.DECREMENT_BUYS -> currentPlayer.buys -= 1
                    PlayerSimpleOperation.INCREMENT_COINS -> currentPlayer.coins += 1
                    PlayerSimpleOperation.DECREMENT_COINS -> currentPlayer.coins -= 1
                    PlayerSimpleOperation.INCREMENT_VP -> currentPlayer.baseVp += 1
                    PlayerSimpleOperation.DECREMENT_VP -> currentPlayer.baseVp -= 1
                }
                else -> throw IllegalArgumentException()
            }
        }
    }

    fun processOperation(operation: Operation) {
        when (operation) {
            is PropertyOperation -> processPropertyOperation(operation)
            is StackOperation -> processStackOperation(operation)
            is StateOperation -> processStateOperation(operation)
            else -> throw NotImplementedError()
        }
    }

    fun processBranchSelection(
        context: BranchContext,
        selection: BranchSelection
    ) {

        branchSelectionHistory.add(Triple(currentPlayer.policy.name, context, selection))

        when (selection.context) {

            BranchContext.ATTACK -> {
                if(selection is AttackSelection) {
                    if(selection.block) {
                        val attack = eventStack.pop()
                        if(attack !is DelayedGameOperation) {
                            throw IllegalStateException()
                        }
                    }
                } else {
                    throw IllegalStateException()
                }
            }

            BranchContext.CELLAR -> {
                if(selection is VisibleCellarSelection) {
                    for(card in selection.cards) {
                        currentPlayer.visibleDiscard(card)
                    }
                    eventStack.push(
                        Branch(context = BranchContext.DRAW, options = selection.cards.size))
                } else if(selection is HiddenCellarSelection) {
                    for(x in 1..selection.cardCount) {
                        currentPlayer.hiddenDiscard()
                    }
                    eventStack.push(
                        Branch(context = BranchContext.DRAW, options = selection.cardCount))
                } else {
                    throw IllegalStateException()
                }
            }

            BranchContext.DRAW -> {
                if(selection is VisibleDrawSelection) {
                    val sortedSelection = selection.cards
                        .sortedByDescending { currentPlayer.unknownCards[it] }
                        .toMutableList()
                    for(x in 1..sortedSelection.size) {
                        val topCard = currentPlayer.knownDeck[0]
                        if(topCard != null) {
                            currentPlayer.visibleDraw(topCard)
                            sortedSelection.remove(topCard)
                            sortedSelection.sortByDescending { currentPlayer.unknownCards[it] }
                        } else {
                            sortedSelection.sortByDescending { currentPlayer.unknownCards[it] }
                            val firstCard = sortedSelection.first()
                            sortedSelection.remove(firstCard)
                            currentPlayer.visibleDraw(firstCard)
                        }
                    }
                } else if(selection is HiddenDrawSelection) {
                    for(x in 1..selection.cardCount) {
                        currentPlayer.hiddenDraw()
                    }
                } else {
                    throw IllegalStateException()
                }
            }
            BranchContext.CHOOSE_ACTION, BranchContext.CHOOSE_TREASURE -> {
                when (selection) {
                    is ActionSelection -> processStackOperation(PlayerCardOperation.PLAY(selection.card))
                    is TreasureSelection -> {
                        for(card in selection.cards) {
                            processStackOperation(PlayerCardOperation.PLAY(card))
                        }
                    }
                    else -> {
                        throw IllegalStateException()
                    }
                }
            }
            BranchContext.CHOOSE_BUY -> {
                if(selection is BuySelection) {
                    for(card in selection.cards) {
                        processStackOperation(PlayerCardOperation.BUY(card))
                    }
                } else {
                    throw IllegalStateException()
                }

            }
            BranchContext.HARBINGER -> {
                if(selection is HarbingerSelection) {
                    if(selection.card != null) {
                        currentPlayer.topdeck(selection.card)
                    }
                } else {
                    throw IllegalStateException()
                }
            }
            BranchContext.VASSAL_DISCARD -> {
                if(selection is VassalDiscardSelection) {
                    currentPlayer.vassalCard = selection.card
                    currentPlayer.identify(selection.card, 0)
                    currentPlayer.visibleDiscardFromDeck(0)
                    if(selection.card.type == CardType.ACTION) {
                        eventStack.push(Branch(context = BranchContext.VASSAL_PLAY))
                    }
                } else {
                    throw IllegalStateException()
                }
            }
            BranchContext.VASSAL_PLAY -> {
                if(selection is VassalPlaySelection) {
                    processOperation(PLAY_FROM_DISCARD(selection.card))
                    currentPlayer.vassalCard = null
                } else {
                    throw IllegalStateException()
                }
            }
            BranchContext.BUREAUCRAT -> {
                if(selection is BureaucratSelection) {
                    currentPlayer.visibleDiscard(selection.card)
                    currentPlayer.topdeck(selection.card)
                } else {
                    throw IllegalStateException()
                }
            }
            BranchContext.MILITIA -> {
                if(selection is VisibleMilitiaSelection) {
                    for(card in selection.cards)
                    currentPlayer.visibleDiscard(card)
                } else if(selection is HiddenMilitiaSelection) {
                    for(x in 1..selection.cardCount) {
                        currentPlayer.hiddenDiscard() // TODO: audit, since not quite hidden
                    }
                } else {
                    throw IllegalStateException()
                }
            }
            BranchContext.POACHER -> {
                if(selection is VisiblePoacherSelection) {
                    currentPlayer.visibleDiscard(selection.card)
                } else if(selection is HiddenPoacherSelection) {
                    currentPlayer.hiddenDiscard()
                } else {
                    throw IllegalStateException()
                }
            }
            BranchContext.CHAPEL -> {
                if(selection is ChapelSelection) {
                    for(card in selection.cards) {
                        currentPlayer.trash(card)
                    }
                } else {
                    throw IllegalStateException()
                }
            }
            BranchContext.REMODEL_TRASH -> {
                if(selection is RemodelTrashSelection) {
                    processOperation(PlayerCardOperation.TRASH(selection.card))
                    processOperation(SetFromPropertyOperation.SET_REMODEL_CARD(currentPlayer.remodelCard, selection.card))
                } else {
                    throw IllegalStateException()
                }
            }
            BranchContext.WORKSHOP, BranchContext.REMODEL_GAIN -> {
                if(selection is WorkshopSelection) {
                    processStackOperation(PlayerCardOperation.GAIN(selection.card))
                } else if(selection is RemodelGainSelection) {
                    processStackOperation(PlayerCardOperation.GAIN(selection.card))
                } else {
                    throw IllegalStateException()
                }
            }
            BranchContext.THRONE_ROOM -> {
                if(selection is ThroneRoomSelection) {
                    eventStack.push(DelayedGameOperation(
                        operation = {
                            processStackOperation(PlayerCardOperation.PLAY_WITH_THRONE(selection.card))
                        }
                    ))
                    processStackOperation(PlayerCardOperation.PLAY(selection.card))
                } else {
                    throw IllegalStateException()
                }
            }
            BranchContext.BANDIT -> {
                if(selection is BanditSelection) {
                    val first = currentPlayer.knownDeck[0]!!
                    val second = currentPlayer.knownDeck[1]!!
                    currentPlayer.visibleDraw(first)
                    currentPlayer.visibleDraw(second)
                    if(first == selection.card) {
                        currentPlayer.trash(first)
                        currentPlayer.visibleDiscard(second)
                    } else if(second == selection.card) {
                        currentPlayer.trash(second)
                        currentPlayer.visibleDiscard(first)
                    } else {
                        throw IllegalStateException()
                    }
                } else {
                    throw IllegalStateException()
                }
            }
            BranchContext.LIBRARY_IDENTIFY -> { // TODO: don't look unless there are less than seven cards in hand
                if(selection is VisibleLibraryIdentifySelection) {
                   currentPlayer.identify(selection.card, 0)
                } else if(selection is HiddenLibraryIdentifySelection) {
                    // DO NOTHING
                } else {
                    throw IllegalStateException()
                }
                eventStack.push(Branch(BranchContext.LIBRARY_DRAW))
            }


            BranchContext.LIBRARY_DRAW -> {
                if(selection is VisibleLibrarySkipSelection) {
                    currentPlayer.visibleDiscardFromDeck(0)
                } else if(selection is HiddenLibrarySkipSelection) {
                    currentPlayer.hiddenDiscardFromDeck(0) // TODO: don't need index?
                } else if(selection is VisibleLibraryDrawSelection || selection is HiddenLibraryDrawSelection) {
                    if(currentPlayer.handCount < 6) {
                        eventStack.push(Branch(BranchContext.LIBRARY_IDENTIFY))
                    }
                    eventStack.push(Branch(BranchContext.DRAW))
                } else {
                    throw IllegalStateException()
                }
            }
            BranchContext.MINE_TRASH -> {
                if(selection is MineTrashSelection) {
                    currentPlayer.mineCard = selection.card
                    currentPlayer.trash(currentPlayer.mineCard!!)
                    eventStack.push(Branch(context = BranchContext.MINE_GAIN))
                } else {
                    throw IllegalStateException()
                }
            }
            BranchContext.MINE_GAIN -> {
                if(selection is MineGainSelection) {
                    board[currentPlayer.mineCard!!] -= 1 // TODO:?
                    currentPlayer.gainToHand(currentPlayer.mineCard!!)
                    currentPlayer.mineCard = null
                } else {
                    throw IllegalStateException()
                }
            }
            BranchContext.SENTRY_IDENTIFY -> {
                if(selection is SentryIdentifySelection) {
                    for(pair in selection.cards.sortedBy { it.second }) {
                        if(currentPlayer.deckCount == 1 && pair.second == 1) {
                            val topCard = currentPlayer.knownDeck[0]!!
                            currentPlayer.setAside(0)
                            currentPlayer.shuffle()
                            currentPlayer.discardFromAside(topCard)
                            currentPlayer.topdeck(topCard)
                        }
                        currentPlayer.identify(pair.first, pair.second)
                    }
                    eventStack.push(Branch(context = BranchContext.SENTRY_TRASH))
                } else {
                    throw IllegalStateException()
                }
            }
            BranchContext.SENTRY_TRASH -> {
                if(selection is SentryTrashSelection) {
                    for(pair in selection.cards.toList().sortedByDescending { it.second }) {
                        currentPlayer.trashFromDeck(pair.second)
                    }
                    eventStack.push(Branch(context = BranchContext.SENTRY_DISCARD))
                } else {
                    throw IllegalStateException()
                }
            }
            BranchContext.SENTRY_DISCARD -> {
                if(selection is SentryDiscardSelection) {
                    for(x in 1..selection.cards.size) {
                        currentPlayer.visibleDiscardFromDeck(0)
                    }
                    eventStack.push(Branch(context = BranchContext.SENTRY_TOPDECK))
                } else {
                    throw IllegalStateException()
                }
            }
            BranchContext.SENTRY_TOPDECK -> {
                if(selection is SentryTopdeckSelection) {
                    for((i, pair) in selection.cards.withIndex()) {
                        currentPlayer.identify(pair.first, i)
                    }
                } else {
                    throw IllegalStateException()
                }
            }
            BranchContext.ARTISAN_GAIN -> {
                if(selection is ArtisanGainSelection) {
                    currentPlayer.gainToHand(selection.card)
                    eventStack.push(Branch(context = BranchContext.ARTISAN_TOPDECK))
                } else {
                    throw IllegalStateException()
                }
            }
            BranchContext.ARTISAN_TOPDECK -> {
                if(selection is ArtisanTopdeckSelection) {
                    currentPlayer.topdeckFromHand(selection.card)
                } else {
                    throw IllegalStateException()
                }
            }
            BranchContext.NONE -> {
                when(selection) {
                    is SpecialBranchSelection -> when(selection) {
                        SpecialBranchSelection.SKIP -> processOperation(StackSimpleOperation.SKIP_CONTEXT)
                        SpecialBranchSelection.GAME_OVER -> processOperation(GameSimpleOperation.END_GAME)
                    }
                }
            }
            BranchContext.ANY, BranchContext.GAME_OVER -> throw IllegalStateException()
        }
    }

    fun processEvent(event: GameEvent) {

        if(event is Branch) {

            // shuffle if there aren't any cards on the deck and we need to draw
            if(event.context == BranchContext.DRAW && currentPlayer.deckCount == 0) {
                currentPlayer.shuffle()
            }

            if(event.context == BranchContext.DRAW && event.options > 1 && !log) { // TODO: hacky
                eventStack.pushAll( // TODO: hacky
                    List(event.options) { Branch(BranchContext.DRAW) }
                )
                return processEvent(eventStack.pop())
            }

            processBranchSelection(event.context,
                currentPlayer.policy(this, event))
        } else {
            // TODO: deal with special case
            if(event is SpecialGameEvent) {
                when(event) {
                    SpecialGameEvent.START_GAME -> {
                        eventStack.pushAll(
                            listOf(
                                Branch(context = BranchContext.DRAW, options = 5),
                                SpecialGameEvent.SWITCH_PLAYER,
                                Branch(context = BranchContext.DRAW, options = 5),
                                SpecialGameEvent.SWITCH_PLAYER,
                                SpecialGameEvent.START_TURN
                            ).reversed()
                        )
                    } // TODO: end game sequence review (I think there are paths that aren't used?)
                    SpecialGameEvent.END_GAME -> {
                        eventStack.push(Branch(context = BranchContext.GAME_OVER))
                    }
                    SpecialGameEvent.START_TURN -> {
                        if(eventStack.peek() != null) {
                            throw IllegalStateException()
                        } else {
                            processOperation(SetToPropertyOperation.SET_BUYS(1))
                            processOperation(SetToPropertyOperation.SET_COINS(0))
                            eventStack.push(SpecialGameEvent.END_TURN)
                            eventStack.push(Branch(context = BranchContext.CHOOSE_BUY))
                            eventStack.push(Branch(context = BranchContext.CHOOSE_TREASURE))
                            eventStack.push(Branch(context = BranchContext.CHOOSE_ACTION))
                        }
                    }
                    SpecialGameEvent.END_TURN -> {
                        // TODO: the problem in MCTS implementation is that a game can't be characterized as just a series of branches
                        //       and branch selections. the cleanups, etc. have to happen too.
                        //       -- or maybe it can, since the events are handled "automatically"
                        //       -- check up on this
                        currentPlayer.cleanup()

                        for(card in currentPlayer.knownHand.toList()) {
                            currentPlayer.visibleDiscard(card)
                        }

                        for(cardNumber in 1..currentPlayer.handCount) {
                            currentPlayer.hiddenDiscard()
                        }

                        if(emptyPiles >= 3 || board[Card.PROVINCE] == 0 || turns >= maxTurns) {
                            eventStack.push(SpecialGameEvent.END_GAME)
                        } else {
                            processOperation(GameSimpleOperation.INCREMENT_TURNS)
                            eventStack.push(SpecialGameEvent.START_TURN)
                            eventStack.push(SpecialGameEvent.SWITCH_PLAYER)
                            eventStack.push(Branch(context = BranchContext.DRAW, options = 5))
                        }
                    }
                    SpecialGameEvent.SWITCH_PLAYER -> currentPlayerNumber = otherPlayer.playerNumber
                }
            } else if(event is DelayedGameOperation) {
                event.operation(this)
            } else if(event is AttackEvent) {
                eventStack.pushAll(
                    listOf(
                        Branch(context = BranchContext.ATTACK),
                        event.attack
                    ).reversed()
                )
            } else {
                throw IllegalStateException()
            }
        }
    }

}