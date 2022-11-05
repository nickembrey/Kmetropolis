package engine.operation

interface Operation {

    companion object {
        val NO_OP: Operation = object : Operation {}
    }
}