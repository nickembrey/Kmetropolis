package engine

import java.io.File

// TODO: it would be cool if we could have a setting to make the logs look like dominion.games logs
class DominionLogger(logDirectory: File, private val players: List<String>) {

    // TODO: timing stats

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

    private var gamePlayouts = 0
    private var gameDecisions = 0
    private var gameWinner: String? = null

    private var decisionTime: Double = 0.0
    private var cParameter: Double = 0.0

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

    private fun logGameSummary() {
        log("\nGame summary\n")

        for(player in players) {
            log("$player VP: ${gameVpRecords[player]}")
        }

        log("Playouts: $gamePlayouts")
        log("Decisions: $gameDecisions")

        if(gamePlayouts > 0 && gameDecisions > 0) {
            log("Playouts per decision: ${gamePlayouts / gameDecisions}")
        }

        log("")

    }

    private fun logSimulationSummary() {
        log("Simulation summary\n")

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
        log("Time allotted per decision: $decisionTime")
        log("cParameter: $cParameter")
        log("")
        log("Playouts: $totalPlayouts")
        log("Decisions: $totalDecisions")
        if(totalPlayouts > 0 && totalDecisions > 0) {
            log("Playouts per decision: ${totalPlayouts / totalDecisions}")
        }
    }

    fun addPlayout() {
        gamePlayouts += 1
        totalPlayouts += 1
    }

    fun addDecision() {
        gameDecisions += 1
        totalDecisions += 1
    }

    fun recordGame(gameState: GameState, logSummary: Boolean = true) {

        val playerOne = gameState.playerOne
        val playerTwo = gameState.playerTwo

        gameVpRecords[playerOne.name] = gameVpRecords[playerOne.name]!! + playerOne.vp
        gameVpRecords[playerTwo.name] = gameVpRecords[playerTwo.name]!! + playerTwo.vp
        totalVpRecords[playerOne.name] = totalVpRecords[playerOne.name]!! + playerOne.vp
        totalVpRecords[playerTwo.name] = totalVpRecords[playerTwo.name]!! + playerTwo.vp

        if(playerOne.vp > playerTwo.vp) {
            winRecords[playerOne.name] = winRecords[playerOne.name]!! + 1 // TODO: more elegant way?
            gameWinner = playerOne.name
        } else if(playerTwo.vp > playerOne.vp) {
            winRecords[playerTwo.name] = winRecords[playerTwo.name]!! + 1 // TODO: more elegant way?
            gameWinner = playerTwo.name
        } else {
            winRecords["Ties"] = winRecords["Ties"]!! + 1
        }

        if(logSummary) {
            logGameSummary()
        }

        totalGames += 1
        gameVpRecords[playerOne.name] = 0
        gameVpRecords[playerTwo.name] = 0
        gameWinner = null
        gamePlayouts = 0
        gameDecisions = 0
    }

    fun recordSimulationOptions(decisionTime: Double, cParameter: Double) {
        this.decisionTime = decisionTime
        this.cParameter = cParameter
    }

    fun recordSimulation(logSummary: Boolean = true) {
        if(logSummary) {
            logSimulationSummary()
        }
        write()
    }


}
