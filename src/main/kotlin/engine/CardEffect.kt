package engine

enum class GameEffectType {
    BASIC, ATTACK;
}

data class GameEffect(
    val type: GameEffectType = GameEffectType.BASIC,
    val newContext: ChoiceContext? = null,
    val newContextDecisions: Int = 0,
    val effectFn: ( (GameState) -> Unit )? = null
)

enum class CardEffectTrigger {
    PLAY, GAME_END;
}

enum class CardEffect(
    val trigger: CardEffectTrigger,
    val gameEffect: (GameState) -> GameEffect) {
//    CELLAR_EFFECT(CardEffectTrigger.PLAY, {state ->
//        GameEffect(
//            newContext = ChoiceContext.CELLAR,
//            newContextDecisions = state.choicePlayer.hand.size // TODO: make sure the cellar isn't counted
//        )
//    }),
    CHAPEL_EFFECT(CardEffectTrigger.PLAY, {
        GameEffect(
            newContext = ChoiceContext.CHAPEL,
            newContextDecisions = 4
        )
    }),
    WORKSHOP_EFFECT(CardEffectTrigger.PLAY, {
        GameEffect(
            newContext = ChoiceContext.WORKSHOP,
            newContextDecisions = 1
        )
    }),
    MILITIA_EFFECT(CardEffectTrigger.PLAY, {
        GameEffect(
            type = GameEffectType.ATTACK,
            newContext = ChoiceContext.MILITIA
        )
    }),
    MONEYLENDER_EFFECT(CardEffectTrigger.PLAY, {
        GameEffect(
            effectFn = { state ->
                state.apply {
                    if(currentPlayer.hand.contains(engine.Card.COPPER)) {
                        moveCard(engine.Card.COPPER, currentPlayer.handLocation, engine.CardLocation.TRASH)
                        currentPlayer.coins += 3
                    }
                }
            }
        )
    }),
    REMODEL_EFFECT(CardEffectTrigger.PLAY, {
        GameEffect(
            newContext = ChoiceContext.REMODEL_TRASH,
            newContextDecisions = 1
        )
    }),
    WITCH_EFFECT(CardEffectTrigger.PLAY, {
        GameEffect(
            effectFn = { state ->
                state.apply {
                    moveCard(engine.Card.CURSE, engine.CardLocation.SUPPLY, engine.CardLocation.PLAYER_TWO_DISCARD)
                }
            }
        )
    }),
}

fun GameState.applyEffect(cardEffectFn: (GameState) -> GameEffect) {
    cardEffectFn(this).let { gameEffect ->
        context = gameEffect.newContext ?: context
        contextDecisionCounters = gameEffect.newContextDecisions
        gameEffect.effectFn?.let { it(this) }
    }

}