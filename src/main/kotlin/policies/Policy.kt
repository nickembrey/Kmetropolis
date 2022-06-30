package policies

import engine.*

typealias Policy = (state: GameState, choices: CardChoices) -> Card?