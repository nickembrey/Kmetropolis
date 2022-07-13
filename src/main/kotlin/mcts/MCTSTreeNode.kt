package mcts

import engine.CardChoices
import engine.ChoiceContext
import engine.card.Card
import engine.player.PlayerNumber
import java.util.*
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
    var choices: CardChoices? = Collections.synchronizedList(ArrayList())
    var score: Double = 0.0
    var inProcess: AtomicInteger = AtomicInteger(0)
    var simulations: AtomicInteger = AtomicInteger(0)

    var children: List<MCTSTreeNode> = listOf() // TODO: does this need to be made concurrency safe?
}