package policies.utility

import engine.*

fun _firstChoicePolicy(
    state: GameState,
    choices: CardChoices
): Card? {
    return choices[0]
}