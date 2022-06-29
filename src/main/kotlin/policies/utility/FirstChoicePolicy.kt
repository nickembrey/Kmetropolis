package policies.utility

import engine.*

fun firstChoicePolicy(
    state: GameState,
    player: Player,
    context: ChoiceContext,
    choices: CardChoices
): DecisionIndex {
    return 0
}