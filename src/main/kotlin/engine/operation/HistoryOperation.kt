package engine.operation

import engine.operation.state.StateOperation

// an operation that is logged in operation history
interface HistoryOperation: Operation {
    val undo: StateOperation
}