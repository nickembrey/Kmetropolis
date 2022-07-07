package mcts

import engine.Card
import engine.ChoiceContext
import engine.PlayerNumber
import java.util.concurrent.atomic.AtomicInteger

// TODO: reconsider how we're using wins vs score and optional PolicyPlayer
//  this might be a good place to use a subclass
class MCTSTreeNode( // TODO make a debug version that takes the state and gets all of this stuff
    val parent: MCTSTreeNode?,
    val card: Card?,
    val playerNumber: PlayerNumber?,
    val choiceContext: ChoiceContext?,

) {
    var index: Int? = null
    var score: Double = 0.0
    var inProcess: AtomicInteger = AtomicInteger(0)
    var simulations: AtomicInteger = AtomicInteger(0)

    var children: List<MCTSTreeNode> = listOf() // TODO: does this need to be made concurrency safe?
}