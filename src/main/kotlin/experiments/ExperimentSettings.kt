package experiments

import policies.PolicyName

open class ExperimentSettings(
    val policy1: PolicyName,
    val policy2: PolicyName
)



//class MCTSExperimentSettings( // TODO:
//    policy1: PolicyName,
//    policy2: PolicyName
//): ExperimentSettings(policy1, policy2)