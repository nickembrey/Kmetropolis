package engine.operation.property

import engine.GameState
import engine.branch.BranchContext
import engine.operation.Operation

data class ReadPropertyOperation<T>(
    val readFn: (GameState) -> T,
    val useFn: (T) -> Operation
): PropertyOperation, (GameState) -> Operation {
    override operator fun invoke(state: GameState): Operation = useFn(readFn(state))
}