package policies.policy

import engine.*
import policies._badWitchPolicy
import policies.jansen_tollisen._UCBorigPolicy
import policies.jansen_tollisen._UCTorigPolicy
import policies.rollout._randomPolicy
import policies.rollout.jansen_tollisen._epsilonHeuristicGreedyPolicy
import policies.rollout.jansen_tollisen._heuristicGreedyPolicy
import policies.rollout.jansen_tollisen._randomPolicyWithMPPAF
import policies.utility._firstChoicePolicy

typealias Policy = (state: GameState, choices: CardChoices) -> Card?

// TODO: I think policies will need to be more than functions eventually if we want to store the MCTS Tree in between decisions

val UCTorigPolicy = ::_UCTorigPolicy
val UCBorigPolicy = ::_UCBorigPolicy
val epsilonHeuristicGreedyPolicy = ::_epsilonHeuristicGreedyPolicy
val heuristicGreedyPolicy = ::_heuristicGreedyPolicy
val randomPolicy = ::_randomPolicy
val randomPolicyWithMPPAF = ::_randomPolicyWithMPPAF
val firstChoicePolicy = ::_firstChoicePolicy
val badWitchPolicy = ::_badWitchPolicy