package policies

import engine.*
import policies.rollout.randomPolicy

val simpleSimulatorPolicy = fun(state: GameState, player: Player, context: ChoiceContext, choice: Choice): Decision {
    val scores = (choice.indices).zip(MutableList(choice.size) { 0 })
        .toMap().toMutableMap()
    for(iterations in 1..100) {
        for(possibleDecision in choice.indices) {
            val playerOne = Player(
                "Random 1",
                PlayerNumber.PlayerOne,
                randomPolicy,
                state.playerOne.deck.toMutableList(),
                state.playerOne.hand.toMutableList(),
                state.playerOne.inPlay.toMutableList(),
                state.playerOne.discard.toMutableList()
            )
            val playerTwo = Player(
                "Random 2",
                PlayerNumber.PlayerTwo,
                randomPolicy,
                state.playerTwo.deck.toMutableList(),
                state.playerTwo.hand.toMutableList(),
                state.playerTwo.inPlay.toMutableList(),
                state.playerTwo.discard.toMutableList()
            )
            val simState = GameState(playerOne, playerTwo, state.board.toMutableMap(), state.turns)
            // NOTE we are assuming player two
            playerTwo.makeDecision(simState, Decision(choice, context, possibleDecision))
            while(!simState.gameOver) {
                simState.next()
            }
            if(simState.playerOne.vp < simState.playerTwo.vp) {
                scores[possibleDecision] = scores[possibleDecision]!! + 1
            } else if(simState.playerTwo.vp < simState.playerOne.vp) {
                scores[possibleDecision] = scores[possibleDecision]!! - 1
            }
        }
    }

    return Decision(choice, context, scores.maxByOrNull { it.value }!!.key )
}