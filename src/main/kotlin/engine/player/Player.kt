package engine.player

import engine.card.Card
import engine.performance.util.CardCountMap
import engine.player.cards.PlayerCards
import policies.Policy

// TODO: players should be created and named from within the GameState

data class Player internal constructor(
    val playerNumber: PlayerNumber,
    val policy: Policy,
    private val cards: PlayerCards,
    var buys: Int,
    var coins: Int,
    var baseVp: Int,
    var remodelCard: Card? // TODO: replace with categoricalProperties
): PlayerCards by cards {

    // TODO: avoid getters

    companion object {
        fun new(
            board: CardCountMap,
            playerNumber: PlayerNumber,
            policy: Policy
        ): Player {
            return Player(
                playerNumber = playerNumber,
                policy = policy,
                cards = PlayerCards.new(board, CardCountMap(board.toMap(), mapOf(
                    Card.COPPER to 7,
                    Card.ESTATE to 3
                ))),
                buys = 0,
                coins = 0,
                baseVp = 3,
                remodelCard = null
            )
        }
    }

    val name: String = policy.name.value // TODO: what about self-play?

    fun copyPlayer(newPolicy: Policy): Player {
        return Player(
            playerNumber = playerNumber,
            policy = newPolicy,
            cards = cards.copy(),
            buys = buys,
            coins = coins,
            baseVp = baseVp,
            remodelCard = remodelCard
        )
    }

    val vp // TODO: test
        get() = baseVp + (cards.allCards[Card.GARDENS] * kotlin.math.floor(cards.allCards.size.toDouble() / 10)).toInt()

    // TODO: one way we could make this more functional is by adding the notion of an Effect type,
    //       which playing a card would return and could be passed up to the state to be processed

}