package engine.operation.property

import engine.player.PlayerProperty
import util.memoize

data class ModifyPropertyOperation(
    val target: StateProperty,
    val modification: Int,
): PropertyOperation {
    companion object {
        private fun modifyBuys(by: Int) =
            ModifyPropertyOperation(
                target = PlayerProperty.BUYS,
                modification = by)
        private fun modifyCoins(by: Int) =
            ModifyPropertyOperation(
                target = PlayerProperty.COINS,
                modification = by)
        private fun modifyVp(by: Int) =
            ModifyPropertyOperation(
                target = PlayerProperty.BASE_VP,
                modification = by)

        val MODIFY_BUYS: (Int) -> ModifyPropertyOperation = ::modifyBuys.memoize()
        val MODIFY_COINS: (Int) -> ModifyPropertyOperation = ::modifyCoins.memoize()
        val MODIFY_VP: (Int) -> ModifyPropertyOperation = ::modifyVp.memoize()
    }
}