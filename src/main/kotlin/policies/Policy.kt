package policies

import engine.*

interface Policy {
    val name: String
    fun policy (state: GameState, choices: CardChoices): Card?
}