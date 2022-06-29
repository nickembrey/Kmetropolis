package engine.simulation

import engine.CardChoices
import engine.GameState
import policies.Policy

fun GameState.getNextCardChoices(): CardChoices = context.getCardChoices(choicePlayer, board).let {
    return if(it.size > 1) {
        it
    } else if(it.isEmpty() || it[0] == null) {
        nextPhase()
        getNextCardChoices()
    } else {
        choicePlayer.makeCardDecision(it[0]!!, this, logger)
        getNextCardChoices()
    }
}

fun GameState.makeNextCardDecision(policy: Policy = choicePlayer.defaultPolicy) = getNextCardChoices().let { cardChoices ->
    policy(this, choicePlayer, context, cardChoices).let { decisionIndex ->
        cardChoices[decisionIndex].let { card ->
            if(card == null) {
                nextPhase()
            } else {
                choicePlayer.makeCardDecision(card, this, logger)
            }
        }
    }
}