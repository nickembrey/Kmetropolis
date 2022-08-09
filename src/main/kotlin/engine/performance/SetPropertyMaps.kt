package engine.performance

import engine.GamePhase
import engine.GameProperty
import engine.branch.BranchContext
import engine.operation.property.SetFromPropertyOperation

// TODO: consider moving to companion object
object SetPropertyMaps {
    val phaseMap = GamePhase.values().filter { it != GamePhase.END_GAME }.associateWith { phase ->
        SetFromPropertyOperation(
            target = GameProperty.PHASE,
            from = phase,
            to = phase.next
        )
    }

    private val setContextMap: Map<Pair<BranchContext, BranchContext>, SetFromPropertyOperation<BranchContext>> =
        BranchContext.values().map { from ->
            BranchContext.values().map { to ->
                Pair(from, to)
            }
    }.flatten().associateWith {
            SetFromPropertyOperation(
                target = GameProperty.CONTEXT,
                from = it.first,
                to = it.second
            )
    }
}