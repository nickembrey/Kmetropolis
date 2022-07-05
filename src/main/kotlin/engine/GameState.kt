package engine

import kingdoms.defaultBoard
import policies.Policy
import policies.rollout.jansen_tollisen.epsilonHeuristicGreedyPolicy

// TODO: make some factory methods to make this a little cleaner
class GameState(
    val policies: Pair<Policy, Policy>,
    private val policiesInOrder: Boolean = false,
    val board: Board = defaultBoard,
    var turns: Int = 0,
    var context: ChoiceContext = ChoiceContext.ACTION,
    val trueShuffle: Boolean = true,
    val logger: DominionLogger? = null,
    private val maxTurns: Int = 999,
) {

    fun copy(
        newPolicies: Pair<Policy, Policy> = policies,
        newTrueShuffle: Boolean = trueShuffle,
        newLogger: DominionLogger? = logger,
        newMaxTurns: Int = maxTurns,
        newPoliciesInOrder: Boolean = policiesInOrder,
        obfuscateUnseen: Boolean = false
    ): GameState {

        return GameState(
            policies = newPolicies,
            board = HashMap(board),
            turns = turns,
            context = context,
            trueShuffle = newTrueShuffle,
            logger = newLogger,
            maxTurns = newMaxTurns,
            policiesInOrder = newPoliciesInOrder
        ).also { newState ->
            newState.currentPlayer = when(currentPlayer.playerNumber) {
                PlayerNumber.PlayerOne -> newState.playerOne
                PlayerNumber.PlayerTwo -> newState.playerTwo
            }
        }.also { newState ->

            newState.currentPlayer.inPlay = currentPlayer.inPlay.toMutableList()
            newState.currentPlayer.discard = currentPlayer.discard.toMutableList()
            newState.currentPlayer.inPlay = currentPlayer.inPlay.toMutableList()
            newState.currentPlayer.discard = currentPlayer.discard.toMutableList()
            newState.currentPlayer.hand = currentPlayer.hand.toMutableList()
            newState.currentPlayer.deck = currentPlayer.deck.toMutableList()
            newState.currentPlayer.remodelCard = currentPlayer.remodelCard

            newState.otherPlayer.deck = otherPlayer.deck.toMutableList()
            newState.otherPlayer.hand = otherPlayer.hand.toMutableList()
            newState.otherPlayer.remodelCard = otherPlayer.remodelCard

            if (obfuscateUnseen) {
                newState.currentPlayer.deck.shuffle()

                newState.otherPlayer.deck += newState.otherPlayer.hand
                newState.otherPlayer.hand.clear()
                newState.otherPlayer.deck.shuffle()
                newState.drawCards(otherPlayer.hand.size, newState.otherPlayer)
            }
        }
    }

    private val playerPolicies: List<Policy> = policies.toList().let {
        if(!policiesInOrder) {
            it.shuffled()
        } else {
            it
        }
    }

    val playerOne: Player = Player(
        PlayerNumber.PlayerOne,
        playerPolicies[0],
        when(val it = playerPolicies[0].name) {
            playerPolicies[1].name -> "${it.value} 1"
            else -> it.value
        })

    val playerTwo: Player = Player(
        PlayerNumber.PlayerTwo,
        playerPolicies[1],
        when(val it = playerPolicies[0].name) {
            playerPolicies[1].name -> "${it.value} 2"
            else -> it.value
        })

    val players
        get() = listOf(playerOne, playerTwo)

    val trash: MutableList<Card> = mutableListOf()

    var currentPlayer: Player = playerOne
    val otherPlayer: Player
        get() = if(currentPlayer == playerOne) {
            playerTwo
        } else {
            playerOne
        }

    var concede = false

    var emptySupplyPiles = 0
    var gameOver = false
        get() = field || emptySupplyPiles >= 3 || turns > maxTurns || concede

    val choicePlayer
        get() = if(context == ChoiceContext.MILITIA) {
        otherPlayer
    } else {
        currentPlayer
    }

    // keeps track of how many decisions to spend in a given context
    var contextDecisionCounters: Int = 1
        get() = when(context) {
            ChoiceContext.ACTION -> currentPlayer.actions
            ChoiceContext.TREASURE -> currentPlayer.hand.filter { it.type == CardType.TREASURE }.size
            ChoiceContext.BUY -> currentPlayer.buys
            ChoiceContext.MILITIA -> choicePlayer.hand.size - 3
            else -> field
        }

    fun initialize() {
        playerOne.deck.shuffle()
        playerTwo.deck.shuffle()
        drawCards(5, playerOne, trueShuffle)
        drawCards(5, playerTwo, trueShuffle)
        logger?.startGame(this)
    }

    fun nextContext(exitCurrentContext: Boolean = false) { // TODO: debug
        if(contextDecisionCounters < 1 || exitCurrentContext) {
            contextDecisionCounters = 1
            context = when(context) {
                ChoiceContext.ACTION -> ChoiceContext.TREASURE
                ChoiceContext.TREASURE -> ChoiceContext.BUY
                ChoiceContext.REMODEL_TRASH -> when(exitCurrentContext) {
                    true -> ChoiceContext.ACTION
                    false -> ChoiceContext.REMODEL_GAIN
                }
                ChoiceContext.CHAPEL, ChoiceContext.MILITIA, ChoiceContext.WORKSHOP, ChoiceContext.REMODEL_GAIN -> ChoiceContext.ACTION
                ChoiceContext.BUY -> {
                    endTurn(currentPlayer, trueShuffle, logger)
                    turns += 1
                    currentPlayer = otherPlayer
                    ChoiceContext.ACTION
                }
            }
        }
    }

    fun removeCard(card: Card, from: CardLocation): Card? {
        return when(from) {
            CardLocation.SUPPLY -> {
                board.removeCard(card).also {
                    if(board.getValue(card) == 0) {
                        if(card == Card.PROVINCE) {
                            gameOver = true
                        }
                        emptySupplyPiles += 1
                    }
                }
            }
            CardLocation.TRASH -> trash.removeCard(card)
            CardLocation.PLAYER_ONE_IN_PLAY -> playerOne.inPlay.removeCard(card)
            CardLocation.PLAYER_TWO_IN_PLAY -> playerTwo.inPlay.removeCard(card)
            CardLocation.PLAYER_ONE_HAND -> playerOne.hand.removeCard(card)
            CardLocation.PLAYER_TWO_HAND -> playerTwo.hand.removeCard(card)
            CardLocation.PLAYER_ONE_DISCARD -> playerOne.discard.removeCard(card)
            CardLocation.PLAYER_TWO_DISCARD -> playerTwo.discard.removeCard(card)
        }
    }

    fun addCard(card: Card, to: CardLocation): Card {
        return when(to) {
            CardLocation.SUPPLY -> board.addCard(card)
            CardLocation.TRASH -> trash.addCard(card)
            CardLocation.PLAYER_ONE_IN_PLAY -> playerOne.inPlay.addCard(card)
            CardLocation.PLAYER_TWO_IN_PLAY -> playerTwo.inPlay.addCard(card)
            CardLocation.PLAYER_ONE_HAND -> playerOne.hand.addCard(card)
            CardLocation.PLAYER_TWO_HAND -> playerTwo.hand.addCard(card)
            CardLocation.PLAYER_ONE_DISCARD -> playerOne.discard.addCard(card)
            CardLocation.PLAYER_TWO_DISCARD -> playerTwo.discard.addCard(card)
        }
    }

    fun moveCard(
        card: Card,
        from: CardLocation,
        to: CardLocation,
        validateMove: Boolean = false
    ): Card? {
        return removeCard(card, from)
            ?.let { addCard(it, to) }
            .also {
                if (validateMove && it == null) {
                    throw NoSuchElementException("Card ${card.name} not found at ${from.name}!")
                }
            }
    }

    fun drawCards(number: Int, player: Player, trueShuffle: Boolean = true) {
        if (number > 0) {
            drawCard(player, trueShuffle)
            drawCards(number - 1, player, trueShuffle)
        }
    }

    fun drawCard(player: Player, trueShuffle: Boolean = true) {
        if (player.deck.size == 0) {
            shuffle(player, trueShuffle)
        }
        player.deck.removeFirstOrNull()?.let {
            addCard (it, player.handLocation)
            logger?.log("${player.defaultPolicy.name} draws ${it.name}")
        }

    }

    fun shuffle(player: Player, trueShuffle: Boolean = true) {
        player.deck += player.discard
        player.discard.clear()
        if(trueShuffle) {
            player.deck.shuffle()
        }
    }

    fun endTurn(player: Player, trueShuffle: Boolean = true, logger: DominionLogger? = null) {

        player.discard += player.inPlay
        player.inPlay.clear()
        player.discard += player.hand
        player.hand.clear()
        drawCards(5, player, trueShuffle)
        player.actions = 1
        player.buys = 1
        player.coins = 0
        logger?.log("\n${player.defaultPolicy.name} ends their turn\n")
    }

    fun contextToGameMove(context: ChoiceContext): GameMove {
        return when (context) {
            ChoiceContext.ACTION, ChoiceContext.TREASURE -> GameMove.PLAY
            ChoiceContext.BUY -> GameMove.BUY
            ChoiceContext.CHAPEL -> GameMove.TRASH
            ChoiceContext.MILITIA -> GameMove.DISCARD
            ChoiceContext.WORKSHOP -> GameMove.GAIN
            ChoiceContext.REMODEL_TRASH -> GameMove.TRASH
            ChoiceContext.REMODEL_GAIN -> GameMove.GAIN
        }
    }

    fun processGameMove(player: Player, move: GameMove, card: Card) {
        when (move) {
            GameMove.BUY -> {
                player.coins -= card.cost
                player.buys -= 1
                moveCard(card, CardLocation.SUPPLY, player.discardLocation, true)
                player.baseVp += card.vp
            }
            GameMove.GAIN -> {
                moveCard(card, CardLocation.SUPPLY, player.discardLocation, true)
                player.baseVp += card.vp
            }
            GameMove.PLAY -> {
                moveCard(card, player.handLocation, player.inPlayLocation, true)
                if (card.type == CardType.ACTION) {
                    player.actions -= 1
                }

                // TODO: move to card effects
                player.buys += card.addBuys
                player.actions += card.addActions
                player.coins += card.addCoins

                for (effect in card.effectList) {
                    effect(this)
                }

                drawCards(card.addCards, player, trueShuffle)
            }
            GameMove.TRASH -> {
                moveCard(card, player.handLocation, CardLocation.TRASH, true)
                player.baseVp -= card.vp
            }
            GameMove.DISCARD -> moveCard(card, player.handLocation, player.discardLocation, true)
        }
        logger?.log("${player.defaultPolicy.name} ${move.verb} ${card.name}")
    }

    fun makeCardDecision(card: Card?) {

        val previousContext = context
        val move = contextToGameMove(context)
        card?.let { it ->
            processGameMove(choicePlayer, move, it)
        }

        when(context) {
            ChoiceContext.REMODEL_TRASH -> choicePlayer.remodelCard = card
            ChoiceContext.REMODEL_GAIN -> choicePlayer.remodelCard = null
            else -> {}
        }

        // TODO: the need for management here probably means the contextDecisionCounter needs to be rethought and nextContext should be scrapped or redesigned
        if(context == previousContext) { // decrement the decision counters unless we changed context
            contextDecisionCounters -= 1
            nextContext(card == null)
        }
    }

    fun makeNextCardDecision(policy: Policy = choicePlayer.defaultPolicy) {
        context.getCardChoices(choicePlayer, board).let {
            when(it.size) {
                1 -> makeCardDecision(it[0])
                else -> makeCardDecision(policy.policy(this, it))
            }
        }
    }

}