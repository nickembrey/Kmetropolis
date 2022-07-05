package engine

import policies.PolicyName
import util.SimulationTimer
import java.io.File

// TODO: it would be cool if we could have a setting to make the logs look like dominion.games logs
//       1st we would need tokens for actions and have log be a series of tokens
//       and somehow handle indentation for Dominion.games logs
//       NOTE: this could also be a first step toward making games that are undo-able


class DominionLogger(logDirectory: File) {

    // TODO: timing stats then profile

    private val logFile: File
    private val gameVpRecords: MutableMap<PolicyName, Int> = mutableMapOf()
    private val totalVpRecords: MutableMap<PolicyName, Int> = mutableMapOf()
    private val winRecords: MutableMap<PolicyName, Int> = mutableMapOf()
    private val tieRecords: MutableMap<Pair<PolicyName, PolicyName>, Int> = mutableMapOf()

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

    private var cParameter: Double = 0.0

    private var gamePlayouts = 0
    private var gameDecisions = 0

    private var totalPlayouts = 0
    private var totalDecisions = 0
    private var totalGames = 0

    private var log = StringBuilder()

    fun log(str: String) {
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
            logGameSummary()
        }

        totalGames += 1
        gameVpRecords.remove(playerOne.defaultPolicy.name)
        gameVpRecords.remove(playerTwo.defaultPolicy.name)
        gamePlayouts = 0
        gameDecisions = 0
    }

    fun recordSimulationOptions(cParameter: Double) {
        this.cParameter = cParameter
    }

    fun recordSimulation(logSummary: Boolean = true) {
        if(logSummary) {
            logSimulationSummary()
        }
        write()
    }

    private fun logGameSummary() {
        log("\nGame summary")
        log("")
        for((policy, vp) in gameVpRecords) {
            log("$policy VP: $vp")
        }
    }

    private fun logSimulationSummary() {
        log("Simulation summary\n")
        log("")
        log("cParameter: $cParameter")
        log("")
        for((policy, vp) in totalVpRecords) {
            log("$policy VP: $vp")
        }
        log("")
        for((policy, wins) in winRecords) {
            log("$policy wins: $wins")
        }
        for((pair, ties) in tieRecords) {
            log("${pair.first} vs. ${pair.second} ties: $ties")
        }
        log("")
        log("Total playouts: $totalPlayouts")
        log("Total decisions: $totalDecisions")
        log("Total games: $totalGames")
        log("")
        if(totalPlayouts > 0 && totalDecisions > 0) {
            log("Playouts per decision: ${totalPlayouts.toDouble() / totalDecisions}")
        }
        log("Decisions per game: ${totalDecisions.toDouble() / totalGames}")
        log("")
        if(totalPlayouts > 0 && totalDecisions > 0) {
            log("Average time per playout: ${timer.totalTime.toDouble() / totalPlayouts}")
        }
        log("Average time per decision: ${timer.totalTime.toDouble() / totalDecisions}")
        log("Average time per game: ${timer.totalTime.toDouble() / totalGames}")
    }


}
