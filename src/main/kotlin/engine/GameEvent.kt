package engine

interface GameEvent

data class AttackEvent(val attack: DelayedGameOperation): GameEvent

data class DelayedGameOperation(
    val operation: (GameState) -> Unit
): GameEvent

enum class SpecialGameEvent: GameEvent {
    START_GAME, END_GAME, START_TURN, END_TURN, SWITCH_PLAYER
}

