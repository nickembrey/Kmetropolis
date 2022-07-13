package engine.player

import engine.card.Card
import engine.card.CardLocation
import policies.Policy

// TODO: players should be created and named from within the GameState

data class Player(
    val playerNumber: PlayerNumber,
    val defaultPolicy: Policy,
    val name: String,
    var deck: MutableList<Card> = mutableListOf(
        Card.COPPER,
        Card.COPPER,
        Card.COPPER,
        Card.COPPER,
        Card.COPPER,
        Card.COPPER,
        Card.COPPER,
        Card.ESTATE,
        Card.ESTATE,
        Card.ESTATE
    ),
    var hand: MutableList<Card> = mutableListOf(),
    var inPlay: MutableList<Card> = mutableListOf(),
    var discard: MutableList<Card> = mutableListOf()
) {

    var actions = 1
    var buys = 1
    var coins = 0
    var baseVp = 3

    var remodelCard: Card? = null

    val handLocation: CardLocation
        get() = when(playerNumber) {
            PlayerNumber.PLAYER_ONE -> CardLocation.PLAYER_ONE_HAND
            PlayerNumber.PLAYER_TWO -> CardLocation.PLAYER_TWO_HAND
        }

    val inPlayLocation: CardLocation
        get() = when(playerNumber) {
            PlayerNumber.PLAYER_ONE -> CardLocation.PLAYER_ONE_IN_PLAY
            PlayerNumber.PLAYER_TWO -> CardLocation.PLAYER_TWO_IN_PLAY
        }

    val discardLocation: CardLocation
        get() = when(playerNumber) {
            PlayerNumber.PLAYER_ONE -> CardLocation.PLAYER_ONE_DISCARD
            PlayerNumber.PLAYER_TWO -> CardLocation.PLAYER_TWO_DISCARD
        }

    val allCards
        get() = deck + hand + discard + inPlay

    val vp // TODO: test
        get() = baseVp + (allCards.count { it == Card.GARDENS } * kotlin.math.floor(allCards.size.toDouble() / 10)).toInt()

    // TODO: one way we could make this more functional is by adding the notion of an Effect type,
    //       which playing a card would return and could be passed up to the state to be processed

}