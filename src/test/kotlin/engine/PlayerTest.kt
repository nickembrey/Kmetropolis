package engine

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import policies.policy.firstChoicePolicy

internal class PlayerTest {

    private lateinit var playerOne: Player
    private lateinit var playerTwo: Player
    private lateinit var gameState: GameState

    @BeforeEach
    fun setUp() {
        playerOne = Player("Player One", PlayerNumber.PlayerOne, firstChoicePolicy)
        playerTwo = Player("Player Two", PlayerNumber.PlayerTwo, firstChoicePolicy)
        gameState = GameState(playerOne, playerTwo)
        gameState.initialize()
    }

    @Test
    fun playCardThrowsErrorIfNotInHandTest() {
        assertThrows(IllegalStateException::class.java) { playerOne.playCard(Card.WITCH, gameState) }
    }

    @Test
    fun playCardMovesCardTest() {
        playerOne.hand[0] = Card.MILITIA
        playerOne.playCard(Card.MILITIA, gameState)
        assertEquals(4, playerOne.hand.size)
        assertFalse(playerOne.hand.contains(Card.MILITIA))
        assertEquals(1, playerOne.inPlay.size)
        assertTrue(playerOne.inPlay.contains(Card.MILITIA))
    }

    @Test
    fun playCardChangesPlayerStateTest() {
        playerOne.hand[0] = Card.FESTIVAL
        playerOne.playCard(Card.FESTIVAL, gameState)
        assertEquals(2, playerOne.actions)
        assertEquals(2, playerOne.buys)
        assertEquals(2, playerOne.coins)
    }

    @Test
    fun playCardTriggersEffectTest() {
        playerOne.hand[0] = Card.MILITIA
        playerOne.playCard(Card.MILITIA, gameState)
        assertEquals(ChoiceContext.MILITIA, gameState.context)
    }

    @Test
    fun playCardTriggersPlayerDraw() {
        playerOne.hand[0] = Card.SMITHY
        playerOne.playCard(Card.SMITHY, gameState)
        assertEquals(7, playerOne.hand.size)
    }

    @Test
    fun buyCardTest() {
        playerOne.coins = 5
        playerOne.buyCard(Card.SMITHY, gameState.board)
        assertEquals(1, playerOne.coins)
        assertEquals(0, playerOne.buys)
        assertEquals(1, playerOne.discard.size)
        assertEquals(Card.SMITHY, playerOne.discard[0])
    }

    @Test
    fun gainCard() {
        playerOne.gainCard(Card.SMITHY, gameState.board)
        assertEquals(1, playerOne.discard.size)
        assertEquals(Card.SMITHY, playerOne.discard[0])
    }

    @Test
    fun trashCardThrowsErrorIfNotInHandTest() {
        assertThrows(IllegalStateException::class.java) { playerOne.trashCard(Card.WITCH) }
    }

    @Test
    fun trashCardTest() {
        playerOne.hand[0] = Card.MILITIA
        playerOne.trashCard(Card.MILITIA)
        assertEquals(4, playerOne.hand.size)
        assertFalse(playerOne.hand.contains(Card.MILITIA))
    }

    @Test
    fun discardCardTest() {
        playerOne.hand[0] = Card.MILITIA
        playerOne.discardCard(Card.MILITIA)
        assertEquals(4, playerOne.hand.size)
        assertFalse(playerOne.hand.contains(Card.MILITIA))
        assertEquals(1, playerOne.discard.size)
        assertTrue(playerOne.discard.contains(Card.MILITIA))
    }

    @Test
    fun drawCardsEmptyDeckTest() {
        playerOne.deck = mutableListOf(Card.COPPER)
        playerOne.drawCards(3)
        assertEquals(6, playerOne.hand.size)
        assertEquals(0, playerOne.deck.size)
    }

    @Test
    fun drawCardsTest() {
        playerOne.drawCards(3)
        assertEquals(8, playerOne.hand.size)
        assertEquals(2, playerOne.deck.size)
    }

    @Test
    fun drawCardTriggersShuffleTest() {
        playerOne.deck = mutableListOf()
        playerOne.discard = mutableListOf(Card.COPPER, Card.COPPER, Card.ESTATE)
        playerOne.drawCard()
        assertEquals(2, playerOne.deck.size)
    }

    @Test
    fun drawCardTest() {
        playerOne.drawCard()
        assertEquals(6, playerOne.hand.size)
        assertEquals(4, playerOne.deck.size)
    }

    @Test
    fun shuffleTest() {
        playerOne.discard = mutableListOf(Card.COPPER, Card.COPPER, Card.ESTATE)
        playerOne.shuffle()
        assertEquals(8, playerOne.deck.size)
    }

    @Test
    fun endTurnMovesInPlayToDiscardTest() {
        playerOne.hand = mutableListOf()
        playerOne.inPlay = mutableListOf(Card.COPPER, Card.COPPER, Card.ESTATE)
        playerOne.endTurn(true)
        assertEquals(3, playerOne.discard.size)
    }

    @Test
    fun endTurnMovesHandToDiscardTest() {
        playerOne.endTurn(true)
        assertEquals(5, playerOne.discard.size)
    }

    @Test
    fun endTurnChangesPlayerStateTest() {
        playerOne.actions = 5
        playerOne.buys = 4
        playerOne.coins = 3
        playerOne.endTurn(true)
        assertEquals(1, playerOne.actions)
        assertEquals(1, playerOne.buys)
        assertEquals(0, playerOne.coins)
    }
}