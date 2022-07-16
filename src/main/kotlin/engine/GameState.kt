package engine

import engine.card.*
import engine.player.Player
import engine.player.PlayerNumber
import kingdoms.defaultBoard
import policies.Policy
import stats.DominionLogger

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
                PlayerNumber.PLAYER_ONE -> newState.playerOne
                PlayerNumber.PLAYER_TWO -> newState.playerTwo
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
        PlayerNumber.PLAYER_ONE,
        playerPolicies[0],
        when(val it = playerPolicies[0].name) {
            playerPolicies[1].name -> "${it.value} 1"
            else -> it.value
        })

    val playerTwo: Player = Player(
        PlayerNumber.PLAYER_TWO,
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

    // keeps track of how many total decisions can be made in a given context
    var maxContextDecisions: Int = 1
        get() = when(context) {
            ChoiceContext.ACTION -> currentPlayer.actions
            ChoiceContext.TREASURE -> currentPlayer.hand.filter { it.type == CardType.TREASURE }.size
            ChoiceContext.BUY -> currentPlayer.buys
            ChoiceContext.MILITIA -> choicePlayer.hand.size - 3
            else -> field
        }

    // keeps track of how many decisions have already been made in a given context
    var contextDecisionsMade: Int = 0
        get() = when(context) {
            // NOTE: ACTION, TREASURE, and BUY don't use this system and instead defer to the player state
            ChoiceContext.ACTION, ChoiceContext.TREASURE, ChoiceContext.BUY -> 0
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
        if(contextDecisionsMade == maxContextDecisions || exitCurrentContext) {
            contextDecisionsMade = 0
            maxContextDecisions = 1 // TODO: see if we can remove this
            context = when(context) {
                ChoiceContext.ACTION -> ChoiceContext.TREASURE
                ChoiceContext.TREASURE -> ChoiceContext.BUY
                ChoiceContext.BUY -> {
                    endTurn(currentPlayer, trueShuffle, logger)
                    turns += 1
                    currentPlayer = otherPlayer
                    ChoiceContext.ACTION
                }

                ChoiceContext.CELLAR, ChoiceContext.CHAPEL, ChoiceContext.HARBINGER,
                ChoiceContext.MILITIA, ChoiceContext.WORKSHOP, ChoiceContext.REMODEL_GAIN -> ChoiceContext.ACTION

                ChoiceContext.REMODEL_TRASH -> when(exitCurrentContext) {
                    true -> ChoiceContext.ACTION
                    false -> ChoiceContext.REMODEL_GAIN
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
            CardLocation.PLAYER_ONE_TOPDECK, CardLocation.PLAYER_TWO_TOPDECK -> throw NotImplementedError("Must use drawCard for drawing cards!")
            // TODO: this seems a little hacky, yeah?
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
            CardLocation.PLAYER_ONE_TOPDECK -> playerOne.deck.prependCard(card)
            CardLocation.PLAYER_TWO_TOPDECK -> playerTwo.deck.prependCard(card)
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

    // TODO: this can be a gamemove now
    fun drawCard(player: Player, trueShuffle: Boolean = true): Boolean {
        if (player.deck.size == 0) {
            shuffle(player, trueShuffle)
        }
        val card = player.deck.removeFirstOrNull()?.also {
            addCard (it, player.handLocation)
        }

        return card != null
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

    fun contextToGameMoveType(context: ChoiceContext): GameMoveType {
        return when (context) {
            ChoiceContext.ACTION, ChoiceContext.TREASURE -> GameMoveType.PLAY
            ChoiceContext.BUY -> GameMoveType.BUY
            ChoiceContext.CELLAR, ChoiceContext.MILITIA -> GameMoveType.DISCARD
            ChoiceContext.CHAPEL, ChoiceContext.REMODEL_TRASH -> GameMoveType.TRASH
            ChoiceContext.HARBINGER -> GameMoveType.TOPDECK
            ChoiceContext.WORKSHOP, ChoiceContext.REMODEL_GAIN -> GameMoveType.GAIN
        }
    }

    // TODO: ideally, move, add, remove card would only be used in here
    fun processGameMove(move: GameMove) {

        val player = move.playerTag.getPlayer(this)
        val card = move.card
        if(!move.type.requireSuccess) {
            logger?.log("${player.defaultPolicy.name} ${move.type.verb} ${card.name}")
        }

        val success = when (move.type) {
            GameMoveType.BUY -> {
                player.coins -= card.cost
                player.buys -= 1
                moveCard(card, CardLocation.SUPPLY, player.discardLocation, true) // TODO: we shouldn't need to validate moves right?
                player.baseVp += card.vp
                true // TODO: these should be more elegant
            }
            GameMoveType.DISCARD -> {
                moveCard(card, player.handLocation, player.discardLocation, true)
                true  // TODO: these should be more elegant
            }
            GameMoveType.DRAW -> drawCard(player, trueShuffle)
            GameMoveType.GAIN -> {
                val gained = moveCard(card, CardLocation.SUPPLY, player.discardLocation) != null
                player.baseVp += card.vp
                gained // TODO: these should be more elegant
            }
            GameMoveType.PLAY -> { // TODO: separate function for organization?
                moveCard(card, player.handLocation, player.inPlayLocation, true)
                if (card.type == CardType.ACTION) {
                    player.actions -= 1
                }

                // TODO: move to card effects
                player.buys += card.addBuys
                player.actions += card.addActions
                player.coins += card.addCoins

                // TODO: clean this up with some mapping?
                for (cardEffect in card.cardEffects) {

                    if(cardEffect.trigger == CardEffectTrigger.PLAY) {
                        if(cardEffect.type == CardEffectType.ATTACK) {
                            // TODO: maybe this can be simplified?
                            // TODO: test
                            val attackTriggeredEffects = otherPlayer.hand
                                .flatMap { it.cardEffects }
                                .filter { it.trigger == CardEffectTrigger.ATTACK }
                                .map { it.cardEffectFn(this) }
                            if(!attackTriggeredEffects.any { it.attackResponse == AttackResponse.BLOCK }) {
                                applyEffect(cardEffect.cardEffectFn)
                            }
                        } else {
                            applyEffect(cardEffect.cardEffectFn)
                        }
                    }
                }

                drawCards(card.addCards, player, trueShuffle)
                true // TODO: these should be more elegant
            }
            GameMoveType.TOPDECK -> { // TODO: will probably need more details
                when(currentPlayer.playerNumber) {
                    PlayerNumber.PLAYER_ONE -> moveCard(card, CardLocation.PLAYER_ONE_DISCARD, CardLocation.PLAYER_ONE_TOPDECK)
                    PlayerNumber.PLAYER_TWO -> moveCard(card, CardLocation.PLAYER_TWO_DISCARD, CardLocation.PLAYER_TWO_TOPDECK)
                }
                true // TODO: these should be more elegant
            }
            GameMoveType.TRASH -> {
                if(context == ChoiceContext.REMODEL_TRASH) {
                    choicePlayer.remodelCard = card
                }
                moveCard(card, player.handLocation, CardLocation.TRASH, true)
                player.baseVp -= card.vp
                true // TODO: these should be more elegant
            }
        }
        if(success && move.type.requireSuccess) {
            logger?.log("${player.defaultPolicy.name} ${move.type.verb} ${card.name}")
        }
    }

    fun makeCardDecision(card: Card?) {

        val previousContext = context

        card?.let { it ->
            processGameMove(
                GameMove(
                    choicePlayer.playerNumber,
                    contextToGameMoveType(context),
                    it)
            ) }

        when(context) {
            ChoiceContext.REMODEL_GAIN -> choicePlayer.remodelCard = null
            ChoiceContext.CELLAR -> {
                if(contextDecisionsMade == maxContextDecisions) {
                    drawCards(contextDecisionsMade, currentPlayer)
                }
            }
            else -> {}
        }

        // TODO: the need for management here probably means the contextDecisionCounter needs to be rethought and nextContext should be scrapped or redesigned
        if(context == previousContext) { // decrement the decision counters unless we changed context
            contextDecisionsMade += 1
            nextContext(card == null)
        }
    }

    fun makeNextCardDecision(policy: Policy = choicePlayer.defaultPolicy, distinctChoices: Boolean = true) {

        context.getCardChoices(choicePlayer, board, distinctChoices).let {
            when(it.size) {
                1 -> makeCardDecision(it[0])
                else -> makeCardDecision(policy.policy(this, it))
            }
        }
    }

}