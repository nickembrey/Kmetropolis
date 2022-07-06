package engine

enum class AttackResponse {
    NONE, BLOCK;
}

// TODO: consider changing into subclasses or something?
data class GameEffect(
    val newContext: ChoiceContext? = null,
    val newMaxContextDecisions: Int = 0,
    val attackResponse: AttackResponse = AttackResponse.NONE,
    val gameMove: GameMove? = null, // TODO: might want multiple moves
    val gameEffectFn: ( (GameState) -> Unit )? = null
)