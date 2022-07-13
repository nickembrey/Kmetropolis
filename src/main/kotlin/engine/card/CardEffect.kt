package engine.card

import engine.*
import engine.player.PlayerRole

enum class CardEffectType {
    BASIC, ATTACK;
}

enum class CardEffectTrigger {
    PLAY, ATTACK, GAME_END;
}

enum class CardEffect(
    val type: CardEffectType = CardEffectType.BASIC,
    val trigger: CardEffectTrigger = CardEffectTrigger.PLAY,
    val cardEffectFn: (GameState) -> GameEffect
) {
    CELLAR_EFFECT(cardEffectFn = { state ->
        GameEffect(
            newContext = ChoiceContext.CELLAR,
            newMaxContextDecisions = state.choicePlayer.hand.size // TODO: make sure the cellar isn't counted
        )
    }),
    CHAPEL_EFFECT(cardEffectFn = {
        GameEffect(
            newContext = ChoiceContext.CHAPEL,
            newMaxContextDecisions = 4
        )
    }),
    MOAT_EFFECT(
        trigger = CardEffectTrigger.ATTACK,
        cardEffectFn = {
            GameEffect(
                attackResponse = AttackResponse.BLOCK
            )
        }),
    HARBINGER_EFFECT(cardEffectFn = {
            GameEffect(
                newContext = ChoiceContext.HARBINGER,
                newMaxContextDecisions = 1
            )
        }),
    WORKSHOP_EFFECT(cardEffectFn = {
        GameEffect(
            newContext = ChoiceContext.WORKSHOP,
            newMaxContextDecisions = 1
        )
    }),
    MILITIA_EFFECT(cardEffectFn = { state ->
        GameEffect(
            newContext = ChoiceContext.MILITIA,
            newMaxContextDecisions = state.choicePlayer.hand.size - 3
        )
    }),
    MONEYLENDER_EFFECT(cardEffectFn = {
        GameEffect(
            gameEffectFn = { state -> // TODO: this could be made into two game moves if we could have a conditional on the game effect
                state.apply {
                    if(currentPlayer.hand.contains(Card.COPPER)) {
                        moveCard(Card.COPPER, currentPlayer.handLocation, CardLocation.TRASH)
                        currentPlayer.coins += 3
                    }
                }
            }
        )
    }),
    REMODEL_EFFECT(cardEffectFn = {
        GameEffect(
            newContext = ChoiceContext.REMODEL_TRASH,
            newMaxContextDecisions = 1
        )
    }),
    WITCH_EFFECT(cardEffectFn = {
        GameEffect(
            gameMove = GameMove(PlayerRole.OTHER_PLAYER, GameMoveType.GAIN, Card.CURSE)
        )
    }),
}

fun GameState.applyEffect(cardEffectFn: (GameState) -> GameEffect) {
    cardEffectFn(this).let { gameEffect ->

        context = gameEffect.newContext?.also { contextDecisionsMade = 0 } ?: context
        maxContextDecisions = gameEffect.newMaxContextDecisions
        gameEffect.gameMove?.let { processGameMove(it) }
        gameEffect.gameEffectFn?.let { it(this) }
    }

}