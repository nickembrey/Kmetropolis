package engine.branch

import engine.card.Card

interface BranchSelection

data class DrawSelection(val cards: List<Card>, val probability: Double): BranchSelection

enum class SpecialBranchSelection: BranchSelection {
    SKIP, GAME_OVER
}