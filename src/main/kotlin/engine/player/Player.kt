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
                    playCard(state, decision)
                }

                state.status = Pair(this, TurnPhase.BUY)
            }
            ChoiceContext.BUY -> {
                if(decision.index != null) {
                    if(decision.index == -1) {
                        print("Stop!")
                    }
                    buyCards(state, decision)
                }
                discard += inPlay
                inPlay = mutableListOf()
                discard += hand
                hand = mutableListOf()
                for (i in 1..5) {
                    drawCard(this, !state.noShuffle)
                }
                state.status = Pair(state.otherPlayer, TurnPhase.ACTION)
            }
            ChoiceContext.CHAPEL -> trashCards(state.currentPlayer, decision)
            ChoiceContext.MILITIA -> {
                if(decision.index != null) {
                    discardCards(state.currentPlayer, decision)
                }
            }
            ChoiceContext.WORKSHOP -> decideGainCard(state.currentPlayer, decision)
        }
        return state
    }

}