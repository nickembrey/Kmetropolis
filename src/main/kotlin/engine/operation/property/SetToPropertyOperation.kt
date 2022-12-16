package engine.operation.property

import engine.player.PlayerProperty
import util.memoize

data class SetToPropertyOperation<T>(
    val target: StateProperty,
    val to: T,
): PropertyOperation {
    companion object {
        private fun setBuys(to: Int) =
            SetToPropertyOperation(
                to = to,
                target = PlayerProperty.BUYS)
        private fun setCoins(to: Int) =
            SetToPropertyOperation(
                to = to,
                target = PlayerProperty.COINS)

        val SET_BUYS: (Int) -> SetToPropertyOperation<Int> = ::setBuys.memoize()
        val SET_COINS: (Int) -> SetToPropertyOperation<Int> = ::setCoins.memoize()
    }
}