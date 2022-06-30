package policies.rollout

import engine.*

fun _randomPolicy(
    state: GameState,
    choices: CardChoices
): Card? {
    return choices.random()
}