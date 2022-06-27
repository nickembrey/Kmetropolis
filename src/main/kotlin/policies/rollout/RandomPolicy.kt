package policies.rollout

import engine.*

fun randomPolicy(
    state: GameState,
    player: Player,
    context: ChoiceContext,
    choices: CardChoices
): DecisionIndex {
    return choices.indices.random()
}