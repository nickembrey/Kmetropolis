package util.input

import engine.card.Card
import engine.card.CardType
import engine.performance.util.CardCountMap

fun defineBoard(): CardCountMap {

    println("")
    println("Board Setup")
    println("")

    val omnipresentCards = listOf(
        Card.PROVINCE,
        Card.DUCHY,
        Card.ESTATE,
        Card.CURSE,
        Card.GOLD,
        Card.SILVER,
        Card.COPPER
    )
    val possibleCards = Card.values().filter { it !in omnipresentCards }
    val chosenCards: MutableList<Card> = mutableListOf()
    chosenCards.addAll(omnipresentCards)
    for(i in 1..10) {
        println("Select board card $i:")
        println("")
        for((index, card) in possibleCards.withIndex()) {
            print("(${index}): $card")
            if((index + 1) % 5 == 0) {
                println("")
            } else {
                for(letter in 1..(20 - card.name.length)) {
                    print(" ")
                }
                if(index < 10) {
                    print(" ")
                }
            }
        }
        println("\n")
        print("Selection: ")
        val chosen = readln().toInt()
        if(chosen !in possibleCards.indices) {
            throw IllegalStateException()
        }
        chosenCards.add(possibleCards[chosen])
        println("")
    }
    return CardCountMap(
        board = chosenCards.associateWith { if(it.type == CardType.ACTION ) 10 else 8 },
        initialValues = chosenCards.associateWith {
            when(it.type) {
                CardType.ACTION, CardType.CURSE -> 10
                CardType.VICTORY -> 8
                CardType.TREASURE -> when(it) {
                    Card.GOLD -> 30
                    Card.SILVER -> 40
                    Card.COPPER -> 46
                    else -> throw IllegalStateException()
                }
            }
        }
    )
}