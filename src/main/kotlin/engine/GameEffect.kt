//package engine
//
//import engine.event.operation.state.GameStateSimpleOperation
//
//enum class AttackResponse {
//    NONE, BLOCK;
//}
//
//// TODO: consider changing into subclasses or something?
//data class GameEffect(
//    val newContext: GameChoicePhase? = null,
//    val newMaxContextDecisions: Int = 0,
//    val attackResponse: AttackResponse = AttackResponse.NONE,
//    val gameMove: GameStateSimpleOperation? = null, // TODO: might want multiple moves
//    val gameEffectFn: ( (GameState) -> Unit )? = null
//)