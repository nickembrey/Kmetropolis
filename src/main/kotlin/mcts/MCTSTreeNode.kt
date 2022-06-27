package mcts

import engine.CardChoices
import engine.ChoiceContext
import engine.PlayerNumber

// TODO: reconsider how we're using wins vs score and optional PolicyPlayer
//  this might be a good place to use a subclass
class MCTSTreeNode( // TODO make a debug version that takes the state and gets all of this stuff
    val parent: MCTSTreeNode? = null,
    val decision: Int? = null,
    val player: PlayerNumber? = null,
    val choice: Any? = null,
    val context: ChoiceContext? = null
) {
    // TODO: maybe we should get rid of one of these or do something clever to unify them
    var wins: Int = 0
    var score: Double = 0.0
    var simulations: Int = 0

    var children: MutableList<MCTSTreeNode> = mutableListOf()

    fun addChild(
        decision: Int,
        player: PlayerNumber? = null,
        choice: Any? = null,
        context: ChoiceContext? = null
    ) {
        children.add(MCTSTreeNode(this, decision, player, choice, context))
    }
}