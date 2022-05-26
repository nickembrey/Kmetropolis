package engine.player

import GameState
import engine.Card
import engine.Choice
import engine.ChoiceContext
import engine.Decision

val randomPolicy = fun(state: GameState, player: Player, context: ChoiceContext, choice: Choice): Decision {
    return Decision(choice, context, choice.indices.random())
}