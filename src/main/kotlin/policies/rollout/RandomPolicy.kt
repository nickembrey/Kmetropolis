package policies.rollout

import engine.*

fun randomPolicy(
    state: GameState,
    player: Player,
    context: ChoiceContext,
    choices: CardChoices
): Decision {
    return Decision(choices.choices.indices.random())
}