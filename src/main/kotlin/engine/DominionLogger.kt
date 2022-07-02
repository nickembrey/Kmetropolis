package engine

import util.SimulationTimer
import java.io.File

// TODO: it would be cool if we could have a setting to make the logs look like dominion.games logs
//       1st we would need tokens for actions and have log be a series of tokens
//       and somehow handle indentation for Dominion.games logs
//       NOTE: this could also be a first step toward making games that are undo-able


class DominionLogger(logDirectory: File, private val players: List<String>) {

    // TODO: timing stats then profile

    private val logFile: File
    private val gameVpRecords: MutableMap<String, Int>
    private val totalVpRecords: MutableMap<String, Int>
    private val winRecords: MutableMap<String, Int>

    init {
        val base = "dominion-log"
        var number = 0
        var candidateFile = File(logDirectory, base + number)
        while(candidateFile.exists()) {
            number += 1
            candidateFile = File(logDirectory, base + number)
        }
        logFile = candidateFile

        gameVpRecords = players.associateWith { 0 }.toMutableMap()
        totalVpRecords = players.associateWith { 0 }.toMutableMap()
        winRecords = players.plus("Ties").associateWith { 0 }.toMutableMap()
    }

    private val timer: SimulationTimer = SimulationTimer()

    private var decisionTime: Double = 0.0
    private var cParameter: Double = 0.0

    private var gamePlayouts = 0
    private var gameDecisions = 0
    private var gameWinner: String? = null

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

    fun recordGame(gameState: GameState, logSummary: Boolean = true) {

        val playerOne = gameState.playerOne
        val playerTwo = gameState.playerTwo

        gameVpRecords[playerOne.defaultPolicy.name] = gameVpRecords[playerOne.defaultPolicy.name]!! + playerOne.vp
        gameVpRecords[playerTwo.defaultPolicy.name] = gameVpRecords[playerTwo.defaultPolicy.name]!! + playerTwo.vp
        totalVpRecords[playerOne.defaultPolicy.name] = totalVpRecords[playerOne.defaultPolicy.name]!! + playerOne.vp
        totalVpRecords[playerTwo.defaultPolicy.name] = totalVpRecords[playerTwo.defaultPolicy.name]!! + playerTwo.vp

        if(playerOne.vp > playerTwo.vp) {
            winRecords[playerOne.defaultPolicy.name] = winRecords[playerOne.defaultPolicy.name]!! + 1 // TODO: more elegant way?
            gameWinner = playerOne.defaultPolicy.name
        } else if(playerTwo.vp > playerOne.vp) {
            winRecords[playerTwo.defaultPolicy.name] = winRecords[playerTwo.defaultPolicy.name]!! + 1 // TODO: more elegant way?
            gameWinner = playerTwo.defaultPolicy.name
        } else {
            winRecords["Ties"] = winRecords["Ties"]!! + 1
        }

        if(logSummary) {
            logGameSummary()
        }

        totalGames += 1
        gameVpRecords[playerOne.defaultPolicy.name] = 0
        gameVpRecords[playerTwo.defaultPolicy.name] = 0
        gameWinner = null
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
        for(player in players) {
            log("$player VP: ${gameVpRecords[player]}")
        }

    }

    private fun logSimulationSummary() {
        log("Simulation summary\n")
        log("")
        log("cParameter: $cParameter")
        log("")
        for(player in players) {
            log("$player VP: ${totalVpRecords[player]}")
        }
        log("")
        for(player in players) {
            log("$player wins: ${winRecords[player]}")
        }
        log("Ties: ${winRecords["Ties"]}")
        log("")
        log("Total games: $totalGames")
        log("")
        log("Playouts: $totalPlayouts")
        log("Decisions: $totalDecisions")
        log("")
        if(totalPlayouts > 0 && totalDecisions > 0) {
            log("Playouts per decision: ${totalPlayouts / totalDecisions}")
        }
        log("Playouts per decision: ${totalPlayouts / totalDecisions}")
        log("")
        log("Average time per playout: ${timer.totalTime.toDouble() / totalPlayouts}")
        log("Average time per decision: ${timer.totalTime.toDouble() / totalDecisions}")
    }


}
