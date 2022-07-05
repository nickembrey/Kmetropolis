//package policies.jansen_tollisen
//
//import engine.*
//import engine.Player
//import mcts.MCTSTreeNode
//import policies.Policy
//import policies.PolicyName
//import policies.rollout.jansen_tollisen.epsilonHeuristicGreedyPolicy
//import kotlin.math.ln
//import kotlin.math.sqrt
//
//object UCBorigPolicy : Policy {
//    override val name = PolicyName("UCBorigPolicy")
//    override fun policy(
//        state: GameState,
//        choices: CardChoices
//    ): Card? {
//
//        // play plus plus actions first (MPPAF)
//        if(state.context == ChoiceContext.ACTION) {
//            val addActionsCard = choices.filterNotNull().firstOrNull { it.addActions > 0 }   // TODO:
//            if (addActionsCard != null) {
//                return addActionsCard
//            }
//        } else if(state.context == ChoiceContext.TREASURE) {
//            return choices[0]
//        }
//
//        // TODO: pull out into settings either at Policy or Simulation level or both
//
//        val seconds = 2.0
//        val cParameter = 0.7
//
//        var totalSimulations = 0
//
//        val choiceNodes = choices.map { MCTSTreeNode(
//            parent = null,
//            card = it,
//            playerNumber = state.choicePlayer.playerNumber,
//            choiceContext = state.context
//        ) }
//
//        // TODO: these should be associated with individual players
//        state.logger?.recordSimulationOptions(cParameter)
//
//        // Note that toMutableList is used below to create copies of the current state.
//        // TODO: cleaner method for this
//        fun getNewState(currentState: GameState, shuffle: Boolean = false): GameState {
//            val playerOne = Player(
//                PlayerNumber.PlayerOne,
//                epsilonHeuristicGreedyPolicy, // TODO: handle when both players have same "name"
//                currentState.playerOne.deck.toMutableList().also { if(shuffle)it.shuffle() }, // TODO:
//                currentState.playerOne.hand.toMutableList(),
//                currentState.playerOne.inPlay.toMutableList(),
//                currentState.playerOne.discard.toMutableList()
//            ).apply { remodelCard = currentState.playerOne.remodelCard }
//            val playerTwo = Player(
//                PlayerNumber.PlayerTwo,
//                epsilonHeuristicGreedyPolicy,
//                currentState.playerTwo.deck.toMutableList().also { if(shuffle)it.shuffle() }, // TODO:
//                currentState.playerTwo.hand.toMutableList(),
//                currentState.playerTwo.inPlay.toMutableList(),
//                currentState.playerTwo.discard.toMutableList()
//            ).apply { remodelCard = currentState.playerOne.remodelCard }
//            return GameState(
//                playerOne,
//                playerTwo,
//                HashMap(currentState.board),
//                currentState.turns,
//                currentState.context,
//                trueShuffle = false,
//                maxTurns = 40
//            ).apply { currentPlayer = when(currentState.currentPlayer.playerNumber) {
//                PlayerNumber.PlayerOne -> playerOne
//                PlayerNumber.PlayerTwo -> playerTwo
//            } }
//        }
//
//        fun rollout(simState: GameState): Double {
//
//            while(!simState.gameOver) {
//                simState.makeNextCardDecision(epsilonHeuristicGreedyPolicy)
//            }
//
//            val playerOneVp = simState.playerOne.vp
//            val playerTwoVp = simState.playerTwo.vp
//
//            val playerOneScore = ( if (playerOneVp > playerTwoVp) 1.0 else 0.0 ) +
//                    (simState.playerOne.vp - simState.playerTwo.vp).toDouble() / 100.0
//            val playerTwoScore = ( if (playerTwoVp > playerOneVp) 1.0 else 0.0 ) +
//                    (simState.playerTwo.vp - simState.playerOne.vp).toDouble() / 100.0
//
//            return when (state.choicePlayer.playerNumber) {
//                PlayerNumber.PlayerOne -> playerOneScore
//                PlayerNumber.PlayerTwo -> playerTwoScore
//            }
//
//        }
//
//        fun forward(simState: GameState, simChoices: CardChoices, choiceNodes: List<MCTSTreeNode>) {
//
//            val index = if (choiceNodes.any { it.simulations == 0 }) {
//                choiceNodes.indexOfFirst { it.simulations == 0 }
//            } else {
//                choiceNodes.map {
//                    (it.score / it.simulations) +
//                            (cParameter * sqrt(ln(totalSimulations.toDouble()) / it.simulations))
//                }.let { values ->
//                    values.indices.maxByOrNull { values[it] }!!
//                }
//            }
//
//            simChoices[index].let {
//                simState.makeCardDecision(it)
//            }
//
//            choiceNodes[index].score += rollout(simState)
//            choiceNodes[index].simulations += 1
//            totalSimulations += 1
//        }
//
//        val iterations = 100000
//        for (it in 1..iterations) {
////        state.logger?.addPlayout() // TODO
//            forward(getNewState(state), choices, choiceNodes)
//        }
//
////    state.logger?.addDecision() // TODO
//
//        val simulations: List<Int> = choiceNodes.map { it.simulations }
//        val maxSim = simulations.indices.maxByOrNull { simulations[it] }!!
//        return choices[maxSim]
//    }
//}
//
