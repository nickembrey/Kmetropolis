package policies.rollout

import engine.*

fun randomPolicy(
    state: GameState,
    choices: CardChoices
): Card? {
    return choices.random()
}