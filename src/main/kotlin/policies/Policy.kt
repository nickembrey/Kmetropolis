package policies

import engine.*

// TODO: we can simplify the policy signature to just require the gameState maybe?
typealias Policy = (state: GameState, player: Player, context: ChoiceContext, choices: CardChoices) -> DecisionIndex