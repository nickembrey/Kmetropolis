//package policies
//
//import engine.GameState
//import engine.branch.BranchContext
//import engine.branch.BranchSelection
//import engine.branch.SpecialBranchSelection
//import engine.operation.stack.StackOperation
//import engine.player.PlayerNumber
//import logger
//import mcts.*
//
//class BackwardMCTSPolicy(
//    cParameter: Double,
//    rollouts: Int,
//    useNBCWeights: Boolean
//): MCTSPolicy(
//    cParameter = cParameter,
//    rollouts = rollouts,
//    useNBCWeights = useNBCWeights
//) {
//    override val name = PolicyName("defaultMCTSPolicy ($cParameter, $rollouts, $useNBCWeights)")
//
//    override val stateCopies: Int = 1
//    override val executor = CurrentThreadExecutor()
//
//    override fun shutdown() {}
//
//    override fun prepareRollout(node: MCTSChildNode, simState: GameState) {
//
//        if(node.depth > maxDepth) maxDepth = node.depth
//        if(node.turns > maxTurns) maxTurns = node.turns
//        contextMap.merge(node.context, 1, Int::plus)
//
//        for(op in node.history) {
//            simState.processOperation(op)
//        }
//        simState.eventStack = node.eventStack.copy()
////        node.history.clear() // TODO: why was this breaking things?
//        // TODO: clear eventStack, or transform node into non-leaf type
//
//
//        if(node is DecisionChildNode) { // only playout decisions
//            backpropagate(node, BackpropProperty.IN_PROCESS)
//            nodeList.addNode(node)
//            val rolloutResult = rollout(simState)
//            rolloutResults.addResult(node.index, rolloutResult)
//        } else {
//            rolloutResults.add(NO_ROLLOUT)
//        }
//    }
//
//    override fun rollout(rolloutState: GameState): Map<PlayerNumber, Double> {
//        logger.initPlayout() // TODO: this might not make sense if there's a timer, etc.
//        while (!rolloutState.gameOver) {
//            rolloutState.processNextBranch()
//        }
//        logger.recordPlayout()
//
//
//        // TODO: see note in PlayerCards
////        rolloutState.undoOperationHistory()
////        stateQueue.add(rolloutState)
//
//        return rolloutState.players.associate {
//            val opponentVp = it.playerNumber.getOpponent(rolloutState).vp
//            it.playerNumber to (it.vp - opponentVp) / 100.0 + if (it.vp > opponentVp) 1.0 else 0.0
//        }
//    }
//
//    private fun backward(node: MCTSChildNode) {
//        val sibling = node.parent.children.firstOrNull {
//            it.completedRollouts.equals(0) && it is DecisionChildNode
//        }
//        if(sibling == null && node.parent is MCTSChildNode) {
//            backward(node.parent)
//        } else if(sibling != null) {
//            nodeList.addNode(sibling)
//            rolloutResults.add(RolloutSelection(
//                index = sibling.index
//            ))
//        } else {
//            throw NotImplementedError()
//        }
//    }
//
//    override fun forward(
//        simState: GameState,
//        nodeArg: MCTSTreeNode
//    ) {
//        var node = nodeArg
//        // TODO: non-recursive?
//        while(true) {
//            if (node.children.isNotEmpty()) {
//                node = getNextNode(node)
//            } else if(node is MCTSChildNode) {
//                simState.processBranchSelection(node.context, node.selection)
//                val selections = getNextOptions(simState)
//                if(selections.size == 1 && selections.single() == SpecialBranchSelection.GAME_OVER) {
//                    backward(node)
//                    break
//                } else {
//                    node.children.addAll(
//                        MCTSChildNode.getChildren(
//                            state = simState,
//                            parent = node,
//                            history = simState.operationHistory.toMutableList(),
//                            eventStack = simState.eventStack.copy(),
//                            selections = selections)
//                    )
//                    node = getNextNode(node)
//                }
//            } else { // TODO: a little hacky
//                throw IllegalStateException()
//            }
//        }
//
//    }
//
//    override fun simulationPolicy(state: GameState): BranchSelection {
//
//        nodeList.clear()
//        rolloutResults.clear()
//        stateQueue.clear()
//
//        maxDepth = 0
//        maxTurns = 0
//
//        val root = RootNode.new(state)
//
//        val shuffledState = state.copy(
//            newPolicies = listOf(GreenRolloutPolicy(), GreenRolloutPolicy()),
//            newMaxTurns = 999, // TODO: think about this
//            newLog = false)
//        stateQueue.add(shuffledState)
//
//        // simulate a redraw of the opponent's hand
//        shuffledState.eventStack.push(
//            StackOperation.OPPONENT_HAND_REDRAW(
//            state.currentPlayerNumber,
//            state.otherPlayer.hand.size)
//        )
//
//        for(i in 2..stateCopies) {
//            stateQueue.add(shuffledState.copy())
//        }
//
//        var queued = 0
//        var draws = 0
//        var processed = 0 // TODO: replace with simulations at root?
//
//        while(processed < rollouts) {
//            while(rolloutResults.isNotEmpty()) {
//                when(val next = rolloutResults.remove()) {
//                    is NO_ROLLOUT -> draws += 1
//                    is RolloutSelection -> {
//                        val node = nodeList[next.index]
//                        backpropagate(node, BackpropProperty.IN_PROCESS)
//                        val rolloutResult = rollout(shuffledState.copy())
//                        rolloutResults.addResult(next.index, rolloutResult)
//                    }
//                    is RolloutResult -> {
//                        val node = nodeList[next.index]
//
//                        backpropagate(node, BackpropProperty.PLAYER_ONE_SCORE, next.scores[PlayerNumber.PLAYER_ONE]!!) // TODO:
//                        backpropagate(node, BackpropProperty.PLAYER_TWO_SCORE, next.scores[PlayerNumber.PLAYER_TWO]!!)
//                        backpropagate(node, BackpropProperty.IN_PROCESS, -1)
//                        backpropagate(node, BackpropProperty.SIMULATIONS)
//
//                        processed += 1
//                    }
//                    else -> throw IllegalStateException()
//                }
//            }
//
//            if(queued - draws < rollouts) {
//                executor.execute {
//                    forward(
//                        nodeArg = root,
//                        simState = shuffledState.copy()
//                    ) }
//                queued += 1
//            }
//        }
//
//        val simulations: List<Int> = root.children.map { it.completedRollouts.get() }
//        val index = simulations.indices.maxByOrNull { simulations[it] }!!
//
//        assert(rolloutResults.isEmpty()) // TODO: make sure will fail
//        nodeList.clear()
//
//        return root.children[index].selection // TODO: hacky
//    }
//}