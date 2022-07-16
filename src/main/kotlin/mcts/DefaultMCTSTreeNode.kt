package mcts

import engine.CardChoices
import engine.ChoiceContext
import engine.GameState
import engine.card.Card
import engine.player.PlayerNumber
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

// TODO: reconsider how we're using wins vs score and optional PolicyPlayer
//  this might be a good place to use a subclass
// TODO: make constructor private
class DefaultMCTSTreeNode constructor( // TODO make a debug version that takes the state and gets all of this stuff
    val parent: DefaultMCTSTreeNode?,
    val cards: List<Card?>,
    val playerNumber: PlayerNumber?, // the player who made this decision
    val choiceContext: ChoiceContext?, // the context for which the decision was made
    var choices: CardChoices? = Collections.synchronizedList(ArrayList())
    ) {

    companion object {
        fun getRoot(state: GameState): DefaultMCTSTreeNode {
            return DefaultMCTSTreeNode(
                parent = null,
                cards = listOf(),
                playerNumber = state.choicePlayer.playerNumber,
                choiceContext = state.context,
                choices = state.context.getCardChoices(state.choicePlayer, state.board)
            )
        }
    }

    var children: List<DefaultMCTSTreeNode> = listOf() // TODO: does this need to be made concurrency safe?

    fun addChildren(children: List<DefaultMCTSTreeNode>) {
        this.children = children
    }

    var index: Int? = null

    var score: Double = 0.0 // the original score, which could be, e.g., number of wins or vp difference

    var inProcess: AtomicInteger = AtomicInteger(0)
    var simulations: AtomicInteger = AtomicInteger(0)
}