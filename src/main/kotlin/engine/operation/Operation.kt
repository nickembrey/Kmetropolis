package engine.operation

import engine.GameEvent

interface Operation: GameEvent {
    override val event: Operation
        get() = this

    companion object {
        val NO_OP: Operation = object : Operation {}
    }
}