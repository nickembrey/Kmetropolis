package policies.policy

import engine.*
import policies._badWitchPolicy
import policies.jansen_tollisen._UCTorigPolicy
import policies.rollout._randomPolicy
import policies.rollout.jansen_tollisen._epsilonHeuristicGreedyPolicy
import policies.rollout.jansen_tollisen._heuristicGreedyPolicy
import policies.utility._firstChoicePolicy

typealias Policy = (state: GameState, choices: CardChoices) -> Card?

val UCTorigPolicy = ::_UCTorigPolicy
val epsilonHeuristicGreedyPolicy = ::_epsilonHeuristicGreedyPolicy
val heuristicGreedyPolicy = ::_heuristicGreedyPolicy
val randomPolicy = ::_randomPolicy
val firstChoicePolicy = ::_firstChoicePolicy
val badWitchPolicy = ::_badWitchPolicy