package engine.branch

import engine.ContextBearer
import engine.card.Card

interface BranchSelection : ContextBearer

data class AttackSelection(val block: Boolean): BranchSelection {
    override val context: BranchContext
        get() = BranchContext.ATTACK
}

data class HiddenCellarSelection(val cardCount: Int): BranchSelection {
    override val context: BranchContext
        get() = BranchContext.CELLAR
}

data class VisibleCellarSelection(val cards: List<Card>): BranchSelection {
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

data class ChapelSelection(val cards: List<Card>): BranchSelection {
    override val context: BranchContext
        get() = BranchContext.CHAPEL
}

data class HarbingerSelection(val card: Card?): BranchSelection {
    override val context: BranchContext
        get() = BranchContext.HARBINGER
}

data class VassalDiscardSelection(val card: Card): BranchSelection {
    override val context: BranchContext
        get() = BranchContext.VASSAL_DISCARD
}

data class VassalPlaySelection(val card: Card): BranchSelection {
    override val context: BranchContext
        get() = BranchContext.VASSAL_PLAY
}

data class WorkshopSelection(val card: Card): BranchSelection {
    override val context: BranchContext
        get() = BranchContext.WORKSHOP
}

data class BureaucratSelection(val card: Card): BranchSelection {
    override val context: BranchContext
        get() = BranchContext.BUREAUCRAT
}

data class HiddenMilitiaSelection(val cardCount: Int): BranchSelection {
    override val context: BranchContext
        get() = BranchContext.MILITIA
}

data class VisibleMilitiaSelection(val cards: List<Card>): BranchSelection {
    override val context: BranchContext
        get() = BranchContext.MILITIA
}

class HiddenPoacherSelection: BranchSelection {
    override val context: BranchContext
        get() = BranchContext.POACHER
}

data class VisiblePoacherSelection(val card: Card): BranchSelection {
    override val context: BranchContext
        get() = BranchContext.POACHER
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

class HiddenLibraryIdentifySelection: BranchSelection {
    override val context: BranchContext
        get() = BranchContext.LIBRARY_IDENTIFY
}

data class VisibleLibraryIdentifySelection(val card: Card): BranchSelection {
    override val context: BranchContext
        get() = BranchContext.LIBRARY_IDENTIFY
}

class HiddenLibrarySkipSelection: BranchSelection {
    override val context: BranchContext
        get() = BranchContext.LIBRARY_DRAW
}

class HiddenLibraryDrawSelection: BranchSelection {
    override val context: BranchContext
        get() = BranchContext.LIBRARY_DRAW
}

data class VisibleLibrarySkipSelection(val card: Card): BranchSelection {
    override val context: BranchContext
        get() = BranchContext.LIBRARY_DRAW
}

data class VisibleLibraryDrawSelection(val card: Card): BranchSelection {
    override val context: BranchContext
        get() = BranchContext.LIBRARY_DRAW
}

data class MineTrashSelection(val card: Card): BranchSelection {
    override val context: BranchContext
        get() = BranchContext.MINE_TRASH
}

data class MineGainSelection(val card: Card): BranchSelection {
    override val context: BranchContext
        get() = BranchContext.MINE_GAIN
}

data class SentryIdentifySelection(val cards: List<Pair<Card, Int>>): BranchSelection {
    override val context: BranchContext
        get() = BranchContext.SENTRY_IDENTIFY
}

data class SentryTrashSelection(val cards: List<Pair<Card, Int>>): BranchSelection {
    override val context: BranchContext
        get() = BranchContext.SENTRY_TRASH
}

data class SentryDiscardSelection(val cards: List<Pair<Card, Int>>): BranchSelection {
    override val context: BranchContext
        get() = BranchContext.SENTRY_DISCARD
}

data class SentryTopdeckSelection(val cards: List<Pair<Card, Int>>): BranchSelection {
    override val context: BranchContext
        get() = BranchContext.SENTRY_TOPDECK
}

data class ArtisanGainSelection(val card: Card): BranchSelection {
    override val context: BranchContext
        get() = BranchContext.ARTISAN_GAIN
}

data class ArtisanTopdeckSelection(val card: Card): BranchSelection {
    override val context: BranchContext
        get() = BranchContext.ARTISAN_TOPDECK
}

data class VisibleDrawSelection(val cards: List<Card>): BranchSelection {
    override val context: BranchContext
        get() = BranchContext.DRAW
}

data class HiddenDrawSelection(val cardCount: Int): BranchSelection {
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