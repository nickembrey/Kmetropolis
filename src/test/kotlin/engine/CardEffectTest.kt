package engine

import engine.simulation.makeNextCardDecision
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import policies.utility.firstChoicePolicy
import kotlin.test.assertEquals
import kotlin.test.assertFalse

internal class CardEffectTest {

    private lateinit var playerOne: Player
    private lateinit var playerTwo: Player
    private lateinit var gameState: GameState

    @BeforeEach
    fun setUp() {
        playerOne = Player("Player One", PlayerNumber.PlayerOne, ::firstChoicePolicy)
        playerTwo = Player("Player Two", PlayerNumber.PlayerTwo, ::firstChoicePolicy)
        gameState = GameState(playerOne, playerTwo)
        gameState.initialize()
    }

    @Test
    fun witchEffectDeliversCurseToOpponentTest() {
        playerOne.hand[0] = Card.WITCH
        gameState.makeNextCardDecision()
        assertEquals(Card.CURSE, playerTwo.discard[0])
    }

    @Test
    fun witchEffectRemovesCurseFromBoardTest() {
        playerOne.hand[0] = Card.WITCH
        gameState.makeNextCardDecision()
        assertEquals(9, gameState.board[Card.CURSE])
    }

    @Test
    fun witchEffectRespectsEmptySupplyTest() {
        playerOne.hand[0] = Card.WITCH
        gameState.board[Card.CURSE] = 0
        gameState.makeNextCardDecision()
        assertEquals(0, gameState.board[Card.CURSE])
        assertEquals(0, playerTwo.discard.size)
    }

    @Test
    fun militiaEffectTest() {
        playerOne.hand[0] = Card.MILITIA
        gameState.makeNextCardDecision()
        assertEquals(playerTwo, gameState.choicePlayer)
        assertEquals(ChoiceContext.MILITIA, gameState.context)
        gameState.makeNextCardDecision()
        assertEquals(playerTwo, gameState.choicePlayer)
        assertEquals(ChoiceContext.MILITIA, gameState.context)
        assertEquals(4, playerTwo.hand.size)
        gameState.makeNextCardDecision()
        assertEquals(playerOne, gameState.choicePlayer)
        assertEquals(ChoiceContext.ACTION, gameState.context)
        assertEquals(3, playerTwo.hand.size)
    }

    @Test
    fun moneyLenderDoesNothingIfNoCopperInHandTest() {
        playerOne.hand[0] = Card.MONEYLENDER
        playerOne.hand.removeIf { it == Card.COPPER }
        gameState.makeNextCardDecision()
        assertEquals(0, playerOne.coins)
    }

    @Test
    fun moneyLenderTrashesForThreeCoinTest() { // TODO: implement a trash to keep up with
        playerOne.hand[0] = Card.MONEYLENDER
        playerOne.hand.removeIf { it == Card.COPPER }
        playerOne.hand.add(Card.COPPER)
        val originalHandSize = playerOne.hand.size
        gameState.makeNextCardDecision()
        assertFalse(playerOne.hand.contains(Card.COPPER))
        assertEquals(originalHandSize - 2, playerOne.hand.size) // removes both the copper and the Moneylender
        assertEquals(3, playerOne.coins)
    }

    @Test
    fun chapelEffectTest() {
        playerOne.hand[0] = Card.CHAPEL
        gameState.makeNextCardDecision()
        assertEquals(playerOne, gameState.choicePlayer)
        assertEquals(ChoiceContext.CHAPEL, gameState.context)
        assertEquals(4, playerOne.hand.size)
        gameState.makeNextCardDecision()
        assertEquals(playerOne, gameState.choicePlayer)
        assertEquals(ChoiceContext.CHAPEL, gameState.context)
        assertEquals(3, playerOne.hand.size)
        gameState.makeNextCardDecision()
        assertEquals(playerOne, gameState.choicePlayer)
        assertEquals(ChoiceContext.CHAPEL, gameState.context)
        assertEquals(2, playerOne.hand.size)
        gameState.makeNextCardDecision()
        assertEquals(playerOne, gameState.choicePlayer)
        assertEquals(ChoiceContext.CHAPEL, gameState.context)
        assertEquals(1, playerOne.hand.size)
        gameState.makeNextCardDecision()
        assertEquals(playerOne, gameState.choicePlayer)
        assertEquals(ChoiceContext.BUY, gameState.context)
        assertEquals(0, playerOne.hand.size)
    }

    @Test
    fun workshopEffectTest() {
        playerOne.hand[0] = Card.WORKSHOP
        gameState.makeNextCardDecision()
        assertEquals(playerOne, gameState.choicePlayer)
        assertEquals(ChoiceContext.WORKSHOP, gameState.context)
        val firstCandidateBoardCard: Card = gameState.board.filter { it.key.cost < 5 }.keys.first()
        gameState.makeNextCardDecision()
        assertEquals(9, gameState.board[firstCandidateBoardCard])
        assertEquals(firstCandidateBoardCard, playerOne.discard[0])
        assertEquals(playerOne, gameState.choicePlayer)
        assertEquals(ChoiceContext.ACTION, gameState.context)
    }
}