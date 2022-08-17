package engine.branch

import engine.card.Card

interface BranchSelection

data class DrawSelection(val cards: List<Card>, val probability: Double): BranchSelection

data class BuySelection(val cards: List<Card>): BranchSelection

enum class SpecialBranchSelection: BranchSelection {
    SKIP, GAME_OVER
}