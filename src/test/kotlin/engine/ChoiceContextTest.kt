package engine

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import policies.utility.firstChoicePolicy
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

internal class ChoiceContextTest {

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

    @AfterEach
    fun tearDown() {
    }

    @Test
    fun getCardChoicesNoActionsTest() {
        val choices = ChoiceContext.ACTION.getCardChoices(playerOne, gameState.board)
        assertEquals(1, choices.size)
        assertNull(choices[0])
    }

    @Test
    fun getCardChoicesActionTest() {
        playerOne.hand = mutableListOf(Card.MILITIA, Card.MONEYLENDER, Card.WITCH, Card.COPPER, Card.COPPER)
        val choices = ChoiceContext.ACTION.getCardChoices(playerOne, gameState.board)
        assertEquals(4, choices.size)
        assertContains(choices, Card.MILITIA)
        assertContains(choices, Card.MONEYLENDER)
        assertContains(choices, Card.WITCH)
        assertContains(choices, null)
    }

    @Test
    fun getCardChoicesTreasureTest() {
        playerOne.hand = mutableListOf(Card.COPPER, Card.COPPER, Card.SILVER, Card.GOLD, Card.ESTATE, Card.WITCH)
        val choices = ChoiceContext.TREASURE.getCardChoices(playerOne, gameState.board)
        assertEquals(5, choices.size)
        assertEquals(2, choices.count { it == Card.COPPER })
        assertEquals(1, choices.count { it == Card.SILVER })
        assertEquals(1, choices.count { it == Card.GOLD })
        assertEquals(1, choices.count { it == null })
    }

    @Test
    fun getCardChoicesNoBuysTest() {
        playerOne.buys = 0
        playerOne.coins = 4
        val choices = ChoiceContext.BUY.getCardChoices(playerOne, gameState.board)
        assertEquals(1, choices.size)
        assertNull(choices[0])
    }

    @Test
    fun getCardChoicesBuyTest() {
        playerOne.coins = 4
        gameState.board[Card.ESTATE] = 0
        val choices = ChoiceContext.BUY.getCardChoices(playerOne, gameState.board)
        assertEquals(10, choices.size)
        assertEquals(1, choices.count { it == Card.SMITHY })
        assertEquals(1, choices.count { it == Card.MONEYLENDER })
        assertEquals(1, choices.count { it == Card.MILITIA })
        assertEquals(1, choices.count { it == Card.CHAPEL })
        assertEquals(1, choices.count { it == Card.VILLAGE })
        assertEquals(1, choices.count { it == Card.WORKSHOP })
        assertEquals(1, choices.count { it == Card.SILVER })
        assertEquals(1, choices.count { it == Card.COPPER })
        assertEquals(1, choices.count { it == Card.CURSE })
        assertEquals(1, choices.count { it == null })
        assertFalse(choices.contains(Card.ESTATE))
    }

    @Test
    fun getCardChoicesChapelTest() {
        playerOne.hand = mutableListOf(Card.COPPER, Card.COPPER, Card.ESTATE, Card.WITCH)
        val choices = ChoiceContext.CHAPEL.getCardChoices(playerOne, gameState.board)
        assertEquals(5, choices.size)
        assertEquals(2, choices.count { it == Card.COPPER })
        assertEquals(1, choices.count { it == Card.ESTATE })
        assertEquals(1, choices.count { it == Card.WITCH })
        assertEquals(1, choices.count { it == null })
    }

    @Test
    fun getCardChoicesNoEffectMilitiaTest() {
        playerOne.hand = mutableListOf(Card.COPPER, Card.ESTATE, Card.WITCH)
        val choices = ChoiceContext.MILITIA.getCardChoices(playerOne, gameState.board)
        assertEquals(1, choices.size)
        assertNull(choices[0])
    }

    @Test
    fun getCardChoicesMilitiaTest() {
        playerOne.hand = mutableListOf(Card.COPPER, Card.COPPER, Card.ESTATE, Card.WITCH)
        val choices = ChoiceContext.MILITIA.getCardChoices(playerOne, gameState.board)
        assertEquals(4, choices.size)
        assertEquals(2, choices.count { it == Card.COPPER })
        assertEquals(1, choices.count { it == Card.ESTATE })
        assertEquals(1, choices.count { it == Card.WITCH })
    }

    @Test
    fun getCardChoicesWorkshopNoneAvailableTest() {
        gameState.board[Card.ESTATE] = 0
        gameState.board[Card.SMITHY] = 0
        gameState.board[Card.MONEYLENDER] = 0
        gameState.board[Card.MILITIA] = 0
        gameState.board[Card.CHAPEL] = 0
        gameState.board[Card.VILLAGE] = 0
        gameState.board[Card.WORKSHOP] = 0
        gameState.board[Card.SILVER] = 0
        gameState.board[Card.COPPER] = 0
        gameState.board[Card.CURSE] = 0
        val choices = ChoiceContext.WORKSHOP.getCardChoices(playerOne, gameState.board)
        assertEquals(0, choices.size)
    }
    @Test
    fun getCardChoicesWorkshopTest() {
        gameState.board[Card.ESTATE] = 0
        val choices = ChoiceContext.WORKSHOP.getCardChoices(playerOne, gameState.board)
        assertEquals(9, choices.size)
        assertEquals(1, choices.count { it == Card.SMITHY })
        assertEquals(1, choices.count { it == Card.MONEYLENDER })
        assertEquals(1, choices.count { it == Card.MILITIA })
        assertEquals(1, choices.count { it == Card.CHAPEL })
        assertEquals(1, choices.count { it == Card.VILLAGE })
        assertEquals(1, choices.count { it == Card.WORKSHOP })
        assertEquals(1, choices.count { it == Card.SILVER })
        assertEquals(1, choices.count { it == Card.COPPER })
        assertEquals(1, choices.count { it == Card.CURSE })
        assertFalse(choices.contains(null))
        assertFalse(choices.contains(Card.ESTATE))
    }
}