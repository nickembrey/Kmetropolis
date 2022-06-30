package policies.utility

import engine.*

fun firstChoicePolicy(
    state: GameState,
    choices: CardChoices
): Card? {
    return choices[0]
}