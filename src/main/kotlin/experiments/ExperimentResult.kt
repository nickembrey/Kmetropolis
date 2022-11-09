package experiments

import engine.branch.BranchContext
import engine.branch.BranchSelection
import policies.PolicyName

class ExperimentResult(
    val settings: ExperimentSettings,
    val gameLogs: List<List<Triple<PolicyName, BranchContext, BranchSelection>>>,
    val gameResults: Map<PolicyName, List<PlayerGameSummary>>
)