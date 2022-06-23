package policies

import GameState
import engine.*

val randomPolicy = fun(
    state: GameState,
    player: Player,
    context: ChoiceContext,
    choice: Choice
): Decision {
    return Decision(choice, context, choice.indices.random())
}