package engine

enum class GamePhase : GamePropertyValue {
    NOT_STARTED,
    START_GAME,
    ACTION,
    TREASURE,
    BUY,
    END_TURN,
    END_GAME;

    val next: GamePhase
        get() = when(this) {
            NOT_STARTED -> START_GAME
            START_GAME -> ACTION
            ACTION -> TREASURE
            TREASURE -> BUY
            BUY -> END_TURN
            END_TURN -> ACTION
            END_GAME -> throw IllegalCallerException("Can't call next on END_GAME phase!")
        }
}