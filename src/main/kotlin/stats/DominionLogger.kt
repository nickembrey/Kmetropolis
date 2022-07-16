package stats

import engine.GameState
import engine.player.Player
import policies.PolicyName
import util.SimulationTimer
import java.io.File

// TODO: add current commit? and state?

// TODO: it would be cool if we could have a setting to make the logs look like dominion.games logs
//       1st we would need tokens for actions and have log be a series of tokens
//       and somehow handle indentation for Dominion.games logs
//       NOTE: this could also be a first step toward making games that are undo-able

// TODO: break down stats by player in new class
class DominionLogger(logDirectory: File) {

    // TODO: timing stats then profile

    private val logFile: File
    private val gameVpRecords: MutableMap<PolicyName, Int> = mutableMapOf()
    private val totalVpRecords: MutableMap<PolicyName, Int> = mutableMapOf()
    private val winRecords: MutableMap<PolicyName, Int> = mutableMapOf()
    private val tieRecords: MutableMap<Pair<PolicyName, PolicyName>, Int> = mutableMapOf() // TODO: not a pair but a set

    init {
        val base = "dominion-log"
        var number = 0
        var candidateFile = File(logDirectory, base + number)
        while(candidateFile.exists()) {
            number += 1
            candidateFile = File(logDirectory, base + number)
        }
        logFile = candidateFile
    }

    private val timer: SimulationTimer = SimulationTimer()

    private var gamePlayouts = 0
    private var gameDecisions = 0

    private var totalPlayouts = 0
    private var totalDecisions = 0
    private var totalGames = 0

    private var log = StringBuilder()

    fun append(str: String) {
        log.append(str)
    }

    fun appendLine(str: String) {
        log.appendLine(str)
    }

    private fun write() {
        logFile.printWriter().use {
            it.print(log)
        }
    }

    fun startGame(state: GameState) {
        gameVpRecords[state.policies.first.name] = 0
        gameVpRecords[state.policies.second.name] = 0
        totalVpRecords.putIfAbsent(state.policies.first.name, 0)
        totalVpRecords.putIfAbsent(state.policies.second.name, 0)
        winRecords.putIfAbsent(state.policies.first.name, 0)
        winRecords.putIfAbsent(state.policies.second.name, 0)
        tieRecords.putIfAbsent(Pair(state.policies.first.name, state.policies.second.name), 0)
    }

    fun startDecision() {
        timer.start()
    }

    fun endDecision() {
        timer.stop()
        gameDecisions += 1
        totalDecisions += 1
    }

    fun startSimulation() {
    }

    fun endSimulation() {
        gamePlayouts += 1
        totalPlayouts += 1
    }

    fun recordGame(state: GameState, logSummary: Boolean = true) {

        val playerOne = state.playerOne
        val playerTwo = state.playerTwo
        val playerOnePolicyName = state.playerOne.defaultPolicy.name
        val playerTwoPolicyName = state.playerTwo.defaultPolicy.name

        gameVpRecords.merge(playerOnePolicyName, playerOne.vp, Int::plus)
        gameVpRecords.merge(playerTwoPolicyName, playerTwo.vp, Int::plus)
        totalVpRecords.merge(playerOnePolicyName, playerOne.vp, Int::plus)
        totalVpRecords.merge(playerTwoPolicyName, playerTwo.vp, Int::plus)

        if(playerOne.vp > playerTwo.vp) {
            winRecords.merge(playerOnePolicyName, 1, Int::plus)
        } else if(playerTwo.vp > playerOne.vp) {
            winRecords.merge(playerTwoPolicyName, 1, Int::plus)
        } else {
            tieRecords.merge(Pair(playerOnePolicyName, playerTwoPolicyName), 1, Int::plus)
        }

        if(logSummary) {
            logGameSummary(players = Pair(playerOne, playerTwo))
        }

        totalGames += 1
        gameVpRecords.remove(playerOne.defaultPolicy.name)
        gameVpRecords.remove(playerTwo.defaultPolicy.name)
        gamePlayouts = 0
        gameDecisions = 0
    }

    fun recordSimulation(logSummary: Boolean = true) {
        if(logSummary) {
            logSimulationSummary()
        }
        write()
    }

    private fun logGameSummary(
        players: Pair<Player, Player>
    ) {
        appendLine("------------")
        appendLine("Game summary")
        appendLine("------------")
        appendLine("")
        appendLine("Player 1: ${players.first.defaultPolicy.name}")
        appendLine("Deck: ")
        for(entry in players.first.allCards.groupingBy { it }.eachCount()) {
            appendLine("      ${entry.key}: ${entry.value}")
        }
        appendLine("")
        appendLine("Player 2: ${players.second.defaultPolicy.name}")
        appendLine("Deck: ")
        for(entry in players.second.allCards.groupingBy { it }.eachCount()) {
            appendLine("      ${entry.key}: ${entry.value}")
        }
        appendLine("")
        for((policy, vp) in gameVpRecords) {
            appendLine("$policy VP: $vp")
        }
        appendLine("")
    }

    private fun logSimulationSummary() {
        appendLine("------------------")
        appendLine("Simulation summary")
        appendLine("------------------")
        appendLine("")
        for((policy, vp) in totalVpRecords) {
            appendLine("$policy VP: $vp")
        }
        appendLine("")
        for((policy, wins) in winRecords) {
            appendLine("$policy wins: $wins")
        }
        for((pair, ties) in tieRecords) {
            appendLine("${pair.first} vs. ${pair.second} ties: $ties")
        }
        appendLine("")
        appendLine("Total playouts: $totalPlayouts")
        appendLine("Total decisions: $totalDecisions")
        appendLine("Total games: $totalGames")
        appendLine("")
        if(totalPlayouts > 0 && totalDecisions > 0) {
            appendLine("Playouts per decision: ${totalPlayouts.toDouble() / totalDecisions}")
        }
        appendLine("Decisions per game: ${totalDecisions.toDouble() / totalGames}")
        appendLine("")
        if(totalPlayouts > 0 && totalDecisions > 0) {
            appendLine("Average time per playout: ${timer.totalTime.toDouble() / totalPlayouts}")
        }
        appendLine("Average time per decision: ${timer.totalTime.toDouble() / totalDecisions}")
        appendLine("Average time per game: ${timer.totalTime.toDouble() / totalGames}")
    }


}
