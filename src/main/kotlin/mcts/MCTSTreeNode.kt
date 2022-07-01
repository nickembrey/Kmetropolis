package mcts

import engine.Card
import engine.ChoiceContext
import engine.PlayerNumber

// TODO: reconsider how we're using wins vs score and optional PolicyPlayer
//  this might be a good place to use a subclass
class MCTSTreeNode( // TODO make a debug version that takes the state and gets all of this stuff
    val parent: MCTSTreeNode?,
    val card: Card?,
    val playerNumber: PlayerNumber?,
    val choiceContext: ChoiceContext?
) {
    var score: Double = 0.0
    var simulations: Int = 0

    var children: List<MCTSTreeNode> = listOf()
}