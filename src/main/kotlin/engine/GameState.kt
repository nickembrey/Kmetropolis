package engine

import engine.branch.*
import engine.card.*
import engine.operation.GameOperation
import engine.operation.HistoryOperation
import engine.operation.PlayerOperation
import engine.operation.state.player.PlayerSimpleOperation
import engine.operation.Operation
import engine.operation.Operation.Companion.ERASE_HISTORY
import engine.operation.property.PropertyOperation
import engine.operation.stack.*
import engine.operation.state.game.GameSimpleOperation
import engine.operation.property.*
import engine.operation.stack.game.GameCompoundOperation
import engine.operation.stack.player.PlayerCardOperation
import engine.operation.stack.player.PlayerCardOperationType
import engine.operation.state.StateOperation
import engine.operation.state.game.GameCardOperation
import engine.operation.state.game.GameCardOperationType
import engine.operation.state.player.PlayerMoveCardOperation
import engine.operation.state.player.PlayerMoveCardsOperation
import engine.operation.state.player.PlayerOperationType
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
    val board: EnumMap<Card, Int>,
    var currentPlayerNumber: PlayerNumber,
    var phase: GamePhase,
    var eventStack: EventStack,
    var turns: Int,
    val maxTurns: Int,
    var emptyPiles: Int,
    val branchSelectionHistory: MutableList<Triple<PolicyName, BranchContext, BranchSelection>>, // TODO: special data class for this
    val operationHistory: ArrayList<HistoryOperation>,
    private val log: Boolean
) {

    private val playerOne = players[0]
    private val playerTwo = players[1]

    companion object {
        fun new(
            policy1: Policy,
            policy2: Policy,
            board: EnumMap<Card, Int>,
            maxTurns: Int,
            log: Boolean
        ): GameState = GameState(
                players = listOf(policy1, policy2)
                    .shuffled()
                    .mapIndexed { i, policy -> Player.new(board, PlayerNumber.fromInt(i), policy) },
                currentPlayerNumber = PlayerNumber.PLAYER_ONE,
                board = board,
                phase = GamePhase.NOT_STARTED,
                eventStack = EventStack(mutableListOf(SpecialGameEvent.START_GAME)), // TODO: new constructor
                turns = 0,
                maxTurns = maxTurns,
                emptyPiles = 0,
                branchSelectionHistory = ArrayList(500), // TODO: audit capacities
                operationHistory = ArrayList(1200),
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

        assert(newPolicies.size == 2)

        return GameState(
            players = players.copy(newPolicies),
            currentPlayerNumber = currentPlayerNumber,
            board = EnumMap(board),
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
            operationHistory = if(keepHistory) {
                ArrayList(operationHistory).apply { ensureCapacity(1200) }
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
    private val buyMenus: List<ArrayList<Card>> = (0..8)
        .map { coins -> ArrayList(
            board.filter { it.key.cost <= coins && it.value > 0 }.keys
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

        currentPlayerNumber = otherPlayer.playerNumber
        val handSize = currentPlayer.hand.size
        processOperation(
            PlayerMoveCardsOperation(
                cards = currentPlayer.hand,
                from = CardLocation.HAND,
                to = CardLocation.DECK
            )
        )
        currentPlayerNumber = otherPlayer.playerNumber

        eventStack.pushAll( listOf(
            SpecialGameEvent.SWITCH_PLAYER,
            Branch(BranchContext.DRAW, handSize),
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

    private fun moveCard(
        card: Card,
        from: CardLocation,
        to: CardLocation,
    ) {
        // TODO: also, better logging?
        // TODO: make each move add its history, make sure nothing upstream is adding history

        when(from) {
            CardLocation.DECK -> when(to) {
                CardLocation.HAND -> {
                    if(currentPlayer.deckSize == 0) {
                        operationHistory.add(
                            PlayerMoveCardsOperation(
                                cards = currentPlayer.discard.toMutableList(),
                                from = CardLocation.DISCARD,
                                to = CardLocation.DECK
                            )
                        )
                        currentPlayer.shuffle()
                    }
                    currentPlayer.draw(card)
                }
                else -> throw NotImplementedError()
            }
            CardLocation.DISCARD -> when(to) {
                CardLocation.HAND -> {
                    throw NotImplementedError() //TODO:
                }
                CardLocation.DECK -> {
                    currentPlayer.shuffle(card)
                }
                else -> throw NotImplementedError()
            }
            CardLocation.HAND -> when(to) {
                CardLocation.DECK -> currentPlayer.undoDraw(card)
                CardLocation.DISCARD -> currentPlayer.discard(card)
                CardLocation.IN_PLAY -> currentPlayer.play(card)
                CardLocation.TRASH -> currentPlayer.trash(card)
                else -> throw NotImplementedError()
            }
            CardLocation.SUPPLY -> when(to) {
                // TODO: feel like we just do all the board / empty pile management here
                CardLocation.DISCARD -> currentPlayer.gain(card)
                else -> throw NotImplementedError()
            }
            CardLocation.IN_PLAY -> when(to) {
                CardLocation.DISCARD -> currentPlayer.cleanup(card)
                else -> throw NotImplementedError()
            }
            else -> throw NotImplementedError()
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
                    operationHistory.add(SetFromPropertyOperation.SET_BUYS(
                        old, currentPlayer.buys
                    ))
                }
                PlayerProperty.COINS -> {
                    val old = currentPlayer.coins
                    currentPlayer.coins += operation.modification
                    operationHistory.add(SetFromPropertyOperation.SET_COINS(
                        old, currentPlayer.coins
                    ))
                }
                PlayerProperty.BASE_VP -> {
                    val old = currentPlayer.baseVp
                    currentPlayer.baseVp += operation.modification
                    operationHistory.add(SetFromPropertyOperation.SET_VP(
                        old, currentPlayer.baseVp
                    ))
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

                operationHistory.add(operation)
            } // TODO: need to put SetProperty under StateOperations
            is SetToPropertyOperation<*> -> when(operation.target) {
                PlayerProperty.BUYS -> {
                    val old = currentPlayer.buys
                    currentPlayer.buys = operation.to as Int // TODO
                    operationHistory.add(SetFromPropertyOperation.SET_BUYS(
                        old, currentPlayer.buys
                    ))
                }
                PlayerProperty.COINS -> {
                    val old = currentPlayer.coins
                    currentPlayer.coins = operation.to as Int // TODO
                    operationHistory.add(SetFromPropertyOperation.SET_COINS(
                        old, currentPlayer.coins
                    ))
                }
                PlayerProperty.BASE_VP -> {
                    val old = currentPlayer.baseVp
                    currentPlayer.baseVp = operation.to as Int // TODO
                    operationHistory.add(SetFromPropertyOperation.SET_VP(
                        old, currentPlayer.baseVp
                    ))
                }
                PlayerProperty.REMODEL_CARD -> {
                    val old = currentPlayer.remodelCard
                    currentPlayer.remodelCard = operation.to as Card // TODO
                    operationHistory.add(SetFromPropertyOperation.SET_REMODEL_CARD(
                        old, currentPlayer.remodelCard
                    ))
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
                    PlayerCardOperationType.PLAY -> {

                        when(operation.card.type) {
                            CardType.ACTION -> {
                                operation.card.effect(this)
                                if(operation.card.addCards > 0) {
                                    eventStack.push(
                                        Branch(BranchContext.DRAW, selections = operation.card.addCards)
                                    )
                                }
                                if(operation.card.addCoins > 0) {
                                    processOperation(ModifyPropertyOperation.MODIFY_COINS(operation.card.addCoins))
                                }
                                if(operation.card.addBuys > 0) {
                                    processOperation(ModifyPropertyOperation.MODIFY_BUYS(operation.card.addBuys))
                                }
                                eventStack.pushAll(
                                    List(operation.card.addActions) { Branch(BranchContext.CHOOSE_ACTION) }
                                )
                                processOperation(
                                    PlayerMoveCardOperation(
                                        card = operation.card,
                                        from = CardLocation.HAND,
                                        to = CardLocation.IN_PLAY)
                                )
                            }
                            CardType.TREASURE -> {
                                processPropertyOperation(ModifyPropertyOperation.MODIFY_COINS(operation.card.addCoins))
                                processStateOperation(PlayerMoveCardOperation.MOVE_CARD(operation.card, CardLocation.HAND, CardLocation.IN_PLAY))
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
                        processOperation(PlayerMoveCardOperation.MOVE_CARD(operation.card, CardLocation.SUPPLY, CardLocation.DISCARD))
                    }
                    PlayerCardOperationType.TRASH -> {
                        if(operation.card.vp != 0) {
                            processOperation(ModifyPropertyOperation.MODIFY_VP(-operation.card.vp))
                        }
                        processOperation(PlayerMoveCardOperation.MOVE_CARD(operation.card, CardLocation.HAND, CardLocation.TRASH))
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
                StackSimpleOperation.SHUFFLE -> currentPlayer.discard.let { cards ->
                    PlayerMoveCardsOperation.MOVE_CARDS(cards.toList(), CardLocation.DISCARD, CardLocation.DECK)
                }.let { processOperation(it) }
                StackSimpleOperation.CLEANUP_ALL -> currentPlayer.inPlay.let { cards ->
                    PlayerMoveCardsOperation.MOVE_CARDS(cards.toList(), CardLocation.IN_PLAY, CardLocation.DISCARD)
                }.let { processOperation(it) }
                StackSimpleOperation.DISCARD_ALL -> currentPlayer.hand.let { cards ->
                    PlayerMoveCardsOperation.MOVE_CARDS(cards.toList(), CardLocation.HAND, CardLocation.DISCARD)
                }.let { processOperation(it) }
                StackSimpleOperation.SKIP_CONTEXT -> {} // do nothing
            }
        }
    }

    private fun processStateOperation(operation: StateOperation) {

        when(operation) {

            // TODO: is GameOperation

            is GameCardOperation -> when(operation.type) {
                GameCardOperationType.DECREMENT_CARD_SUPPLY -> {
                    val numberLeft = board[operation.card]!! - 1 // TODO:
                    board[operation.card] = numberLeft
                    if(numberLeft == 0) {
                        emptyPiles += 1
                        buyMenus.forEach { it.remove(operation.card) } // TODO:
                    }
                }
                GameCardOperationType.INCREMENT_CARD_SUPPLY -> {
                    val previousNumber = board[operation.card]!!
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
                is PlayerMoveCardOperation -> moveCard(
                    card = operation.card,
                    from = operation.from,
                    to = operation.to) // TODO: log on debug
                is PlayerMoveCardsOperation -> {
                    for(card in operation.cards.toList()) { // TODO:
                        moveCard(
                            card = card,
                            from = operation.from,
                            to = operation.to
                        )
                    }
                }
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
            is StateOperation -> processStateOperation(operation).also {
                operationHistory.add(operation)
            }
            else -> {
                if(operation == ERASE_HISTORY) { // TODO: hack for MCTS! need better design
                    operationHistory.clear()
                } else {
                    throw NotImplementedError()
                }
            }
        }
    }

    fun processBranchSelection(
        context: BranchContext,
        selection: BranchSelection
    ) { // TODO: just do the thing, don't push operation onto stack -- then log the operation into the history

//        // TODO: only at root
//        if(selection != SpecialBranchSelection.SKIP) {
//
//        }

        branchSelectionHistory.add(Triple(currentPlayer.policy.name, context, selection))

        when (selection.context) {

            BranchContext.CELLAR -> {
                if(selection is CellarSelection) {
                    for(card in selection.cards) {
                        processStateOperation(PlayerMoveCardOperation( // TODO: add some shortcuts
                            card = card,
                            from = CardLocation.HAND,
                            to = CardLocation.DISCARD
                        ))
                    }
                    eventStack.push(
                        Branch(context = BranchContext.DRAW, selections = selection.cards.size))
                } else {
                    throw IllegalStateException()
                }
            }

            BranchContext.DRAW -> {
                if(selection is DrawSelection) {
                    for(card in selection.cards) {
                        processOperation(PlayerCardOperation.DRAW(card))
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
            BranchContext.MILITIA -> {
                if(selection is MilitiaSelection) {
                    processStateOperation(PlayerMoveCardOperation( // TODO: add some shortcuts
                        card = selection.card,
                        from = CardLocation.HAND,
                        to = CardLocation.DISCARD
                    ))
                } else {
                    throw IllegalStateException()
                }
            }
            BranchContext.CHAPEL -> {
                if(selection is ChapelSelection) {
                    processStackOperation(PlayerCardOperation.TRASH(selection.card))
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
            if(event.context == BranchContext.DRAW && currentPlayer.deckSize == 0) {
                operationHistory.add(
                    PlayerMoveCardsOperation(
                        cards = currentPlayer.discard.toMutableList(),
                        to = CardLocation.DECK,
                        from = CardLocation.DISCARD
                    ))
                currentPlayer.shuffle()
            }

            if(event.context == BranchContext.DRAW && event.selections > 1 && !log) { // TODO: hacky
                eventStack.pushAll( // TODO: hacky
                    List(event.selections) { Branch(BranchContext.DRAW) }
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
                                Branch(context = BranchContext.DRAW, selections = 5),
                                SpecialGameEvent.SWITCH_PLAYER,
                                Branch(context = BranchContext.DRAW, selections = 5),
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
                        processOperation(StackSimpleOperation.CLEANUP_ALL)
                        processOperation(StackSimpleOperation.DISCARD_ALL)

                        if(emptyPiles >= 3 || board[Card.PROVINCE] == 0 || turns >= maxTurns) {
                            eventStack.push(SpecialGameEvent.END_GAME)
                        } else {
                            processOperation(GameSimpleOperation.INCREMENT_TURNS)
                            eventStack.push(SpecialGameEvent.START_TURN)
                            eventStack.push(SpecialGameEvent.SWITCH_PLAYER)
                            eventStack.push(Branch(context = BranchContext.DRAW, selections = 5))
                        }
                    }
                    SpecialGameEvent.SWITCH_PLAYER -> currentPlayerNumber = otherPlayer.playerNumber
                }
            } else {
                throw IllegalStateException()
            }
        }
    }

}