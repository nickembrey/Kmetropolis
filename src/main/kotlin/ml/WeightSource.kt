package ml

import engine.GameState
import engine.branch.BranchSelection

interface WeightSource {

    fun getWeights(state: GameState, selections: Collection<BranchSelection>): List<Double>
}