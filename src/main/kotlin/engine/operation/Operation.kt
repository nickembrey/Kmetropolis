package engine.operation

import engine.GameEvent

interface Operation {

    companion object {
        val NO_OP: Operation = object : Operation {}
        val ERASE_HISTORY: Operation = object : Operation {} // TODO: this is a hack, need better design
    }
}