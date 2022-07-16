//package mcts
//
//import engine.CardChoices
//import engine.ChoiceContext
//import engine.GameState
//import engine.card.Card
//import engine.player.PlayerNumber
//import java.util.*
//import java.util.concurrent.atomic.AtomicInteger
//
//abstract class MCTSTreeNode constructor(
//    val parent: MCTSTreeNode?,
//) {
//
//    companion object {
//        fun getRoot(state: GameState): MCTSTreeNode {
//            return MCTSTreeNode(
//                parent = null,
//                card = null,
//                playerNumber = state.choicePlayer.playerNumber,
//                choiceContext = state.context,
//                choices = state.context.getCardChoices(state.choicePlayer, state.board)
//            )
//        }
//    }
//
//    var children: List<MCTSTreeNode> = listOf() // TODO: does this need to be made concurrency safe?
//
//    fun addChildren(children: List<MCTSTreeNode>) {
//        this.children = children
//    }
//
//    var index: Int? = null
//
//    var score: Double = 0.0 // the original score, which could be, e.g., number of wins or vp difference
//
//    var value: Double = 0.0 // the score weighted by the number of simulations according to UCT
//
//
//    var inProcess: AtomicInteger = AtomicInteger(0)
//    var simulations: AtomicInteger = AtomicInteger(0)
//}