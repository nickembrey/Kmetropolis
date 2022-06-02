package engine.player

import GameState
import engine.*

data class Player(
    val name: String,
    val policy: (GameState, Player, ChoiceContext, Choice) -> Decision,
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

    fun getDecision(state: GameState, context: ChoiceContext): Decision {
        val choice = context.getChoice(state, this)
        return if(choice.isNotEmpty()) {
            policy(state, this, context, choice)
        } else {
            Decision(choice, context, null)
        }
    }

    fun makeDecision(state: GameState, context: ChoiceContext, decision: Decision): GameState {
        when (context) {
            ChoiceContext.ACTION -> {
                if(decision.choice.isNotEmpty() && decision.index != null) {
                    try {
                        playCard(state, decision)
                    } catch(e: Exception) {
                        print("Stop!")
                    }

                } else {
                    // This is standing in for the treasure phase
                    coins += hand.filter { it.type == CardType.TREASURE}.sumOf { it.addCoins }
                    state.context = ChoiceContext.BUY
                }
            }
            ChoiceContext.BUY -> {
                if(decision.index == null || buys == 0) {
                    endTurn(state)
                } else {
                    buyCard(state, decision.choice[decision.index] as Card)
                }
            }
            ChoiceContext.CHAPEL -> {
                trashCards(state.choicePlayer, decision, state.verbose)
                state.context = ChoiceContext.ACTION
            }
            ChoiceContext.MILITIA -> {
                if(decision.index != null) {
                    discardCards(state.choicePlayer, decision, state.verbose)
                }
                state.context = ChoiceContext.ACTION
            }
            ChoiceContext.WORKSHOP -> {
                decideGainCard(state.choicePlayer, decision, state.verbose)
                state.context = ChoiceContext.ACTION
            }
        }
        return state
    }

}