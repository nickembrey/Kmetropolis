package mcts

import engine.PlayerNumber

// TODO: reconsider how we're using wins vs score and optional PolicyPlayer
//  this might be a good place to use a subclass
class MCTSTreeNode(val parent: MCTSTreeNode? = null, val decision: Int? = null, val player: PlayerNumber? = null) {
    // TODO: maybe we should get rid of one of these or do something clever to unify them
    var wins: Int = 0
    var score: Double = 0.0
    var simulations: Int = 0

    var children: MutableList<MCTSTreeNode> = mutableListOf()

    fun addChild(decision: Int, player: PlayerNumber? = null) {
        children.add(MCTSTreeNode(this, decision, player))
    }
}