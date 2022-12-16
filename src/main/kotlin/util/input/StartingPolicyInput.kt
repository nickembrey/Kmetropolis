package util.input

import engine.card.Card
import engine.card.CardType
import engine.performance.util.CardCountMap
import policies.Policy

fun setStartingPolicy(policy1: Policy, policy2: Policy): Int {

    println("")
    println("Choose a starting policy")
    println("")
    println("(1): ${policy1.name}")
    println("(2): ${policy2.name}")
    println("\n")
    print("Selection: ")
    val chosen = readln().toInt()
    if(chosen !in 1..2) {
        throw IllegalStateException()
    }
    println("")
    return chosen
}