package engine

data class Player(
    val name: String,
    val playerNumber: PlayerNumber,
    val policy: (GameState, Player, ChoiceContext, CardChoices) -> Decision,
    var deck: MutableList<Card> = mutableListOf(
        Card.COPPER,
        Card.COPPER,
        Card.COPPER,
        Card.COPPER,
        Card.COPPER,
        Card.COPPER,
        Card.COPPER,
        Card.ESTATE,
        Card.ESTATE,
        Card.ESTATE
    ),
    var hand: MutableList<Card> = mutableListOf(),
    var inPlay: MutableList<Card> = mutableListOf(),
    var discard: MutableList<Card> = mutableListOf()
    ) {

    var actions = 0
    var buys = 1
    var coins = 0

    val allCards
        get() = deck + hand + discard + inPlay

    val vp
        get() = allCards.sumOf { it.vp }

    fun getDecision(state: GameState, choices: CardChoices): Decision {
        // TODO: these are all from state... seems redundant
        // TODO: choices should never be empty
        return policy(state, this, state.context, choices)
    }

    fun makeDecision(state: GameState, choices: CardChoices, index: Decision): GameState {
        when (state.context) {
            ChoiceContext.ACTION -> {
                val cardChoices = choices as SingleCardChoices // TODO: see if there's a way to untangle this
                if(cardChoices.choices.isNotEmpty()) {
                    playActionCard(state, choices, index)
                } else {
                    state.context = ChoiceContext.TREASURE // TODO: this could be handled by an iterator
                }
            }
            ChoiceContext.TREASURE -> {
                val cardChoices = choices as SingleCardChoices // TODO: see if there's a way to untangle this
                if(cardChoices.choices.isNotEmpty()) {
                    playTreasureCard(state, choices, index)
                } else {
                    state.context = ChoiceContext.BUY
                }
                state.context = ChoiceContext.BUY
            }
            ChoiceContext.BUY -> {
                val cardChoices = choices as SingleCardChoices // TODO: see if there's a way to untangle this
                if(cardChoices.choices.isNotEmpty() && buys != 0) {
                    buyCard(state, choices.choices[index.index]!!)   // TODO:
                } else {
                    endTurn(state)
                }
            }
            ChoiceContext.CHAPEL -> {
                val cardChoices = choices as MultipleCardChoices // TODO: see if there's a way to untangle this
                trashCards(state.choicePlayer, cardChoices, index, state.verbose)
                state.context = ChoiceContext.ACTION
            }
            ChoiceContext.MILITIA -> {
                // TODO: make sure discardCards can handle getting empty list
                val cardChoices = choices as MultipleCardChoices // TODO: see if there's a way to untangle this
                discardCards(state.choicePlayer, cardChoices, index, state.verbose)
                state.context = ChoiceContext.ACTION
            }
            ChoiceContext.WORKSHOP -> {
                val cardChoices = choices as SingleCardChoices // TODO: see if there's a way to untangle this
                decideGainCard(state.choicePlayer, cardChoices, index, state.verbose)
                state.context = ChoiceContext.ACTION
            }
        }
        return state
    }

}