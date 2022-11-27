package engine.branch

import engine.ContextBearer
import engine.card.Card

interface BranchSelection : ContextBearer

data class AttackSelection(val block: Boolean): BranchSelection {
    override val context: BranchContext
        get() = BranchContext.ATTACK
}

data class CellarSelection(val cards: List<Card>): BranchSelection {
    override val context: BranchContext
        get() = BranchContext.CELLAR
}

data class ActionSelection(val card: Card): BranchSelection {
    override val context: BranchContext
        get() = BranchContext.CHOOSE_ACTION
}

data class TreasureSelection(val cards: List<Card>): BranchSelection {
    override val context: BranchContext
        get() = BranchContext.CHOOSE_TREASURE
}

data class ChapelSelection(val card: Card): BranchSelection {
    override val context: BranchContext
        get() = BranchContext.CHAPEL
}

data class HarbingerSelection(val card: Card): BranchSelection {
    override val context: BranchContext
        get() = BranchContext.HARBINGER
}

data class VassalSelection(val card: Card): BranchSelection {
    override val context: BranchContext
        get() = BranchContext.VASSAL
}

data class WorkshopSelection(val card: Card): BranchSelection {
    override val context: BranchContext
        get() = BranchContext.WORKSHOP
}

data class BureaucratSelection(val card: Card): BranchSelection {
    override val context: BranchContext
        get() = BranchContext.BUREAUCRAT
}

data class MilitiaSelection(val card: Card): BranchSelection {
    override val context: BranchContext
        get() = BranchContext.MILITIA
}

data class RemodelTrashSelection(val card: Card): BranchSelection {
    override val context: BranchContext
        get() = BranchContext.REMODEL_TRASH
}

data class RemodelGainSelection(val card: Card): BranchSelection {
    override val context: BranchContext
        get() = BranchContext.REMODEL_GAIN
}

data class ThroneRoomSelection(val card: Card): BranchSelection {
    override val context: BranchContext
        get() = BranchContext.THRONE_ROOM
}

data class BanditSelection(val card: Card): BranchSelection {
    override val context: BranchContext
        get() = BranchContext.BANDIT
}

data class DrawSelection(val cards: List<Card>): BranchSelection {
    override val context: BranchContext
        get() = BranchContext.DRAW
}

data class BuySelection(val cards: List<Card>): BranchSelection {
    override val context: BranchContext
        get() = BranchContext.CHOOSE_BUY
}

enum class SpecialBranchSelection(override val context: BranchContext = BranchContext.NONE): BranchSelection { // TODO: make SWITCH and START_TURN GameEvents
    SKIP, GAME_OVER
}