package stats.binning

import engine.card.Card

object Bins {

    fun getGameStage(board: Map<Card, Int>): GameStage {
        return if(board[Card.PROVINCE]!! < 4 || board.values.filter { it == 0 }.size == 2) {
            GameStage.LATE
        } else if(board[Card.PROVINCE]!! < 8 || board.values.any { it == 0 }) {
            GameStage.MID
        } else {
            GameStage.EARLY
        }
    }

    fun getResourceBin(amount: Double): GenericBin {
        return when {
            amount < 0.05 -> GenericBin.VERY_LOW
            amount < 0.1 -> GenericBin.LOW
            amount < 0.15 -> GenericBin.MID
            amount < 0.2 -> GenericBin.HIGH
            else -> GenericBin.VERY_HIGH
        }

    }

}