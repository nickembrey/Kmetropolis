package engine

import java.io.File

// TODO: it would be cool if we could have a setting to make the logs look like dominion.games logs
class DominionLogger(logDirectory: File, private val players: Collection<String>) {

    private val logFile: File
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

        winRecords = players.plus("Ties").associateWith { 0 }.toMutableMap()
    }

    private var gamePlayouts = 0
    private var gameDecisions = 0
    private var gameWinner: String? = null


    private var totalPlayouts = 0
    private var totalDecisions = 0
    private var totalGames = 0 // TODO: do we need this?
    private var totalWinner = null // TODO: do we need this?

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

        if(gameWinner != null) {
            log("The winner was $gameWinner")
        } else {
            log("The game was a tie")
        }
        log("")

        log("Playouts: $gamePlayouts")
        log("Decisions: $gameDecisions")

        if(gamePlayouts > 0) {
            log("Playouts per decision: ${gamePlayouts / gameDecisions}")
        }

        log("")

    }

    private fun logSimulationSummary() {
        log("Simulation summary\n")

        for(player in players) {
            log("$player wins: ${winRecords[player]}")
        }
        log("Ties: ${winRecords["Ties"]}\n")

        log("Total games: $totalGames\n")

        log("Playouts: $totalPlayouts")
        log("Decisions: $totalDecisions")
        if(totalPlayouts > 0) {
            log("Playouts per decision: ${totalPlayouts / totalDecisions}")
        }
    }

    fun addPlayout() {
        gamePlayouts += 1
        totalPlayouts += 1
    }

    fun addDecision() {
        gameDecisions += 1
        totalPlayouts += 1
    }

    fun recordGame(gameState: GameState, logSummary: Boolean = true) {

        val playerOne = gameState.playerOne
        val playerTwo = gameState.playerTwo

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
        gameWinner = null
        gamePlayouts = 0
        gameDecisions = 0
    }

    fun recordSimulation(logSummary: Boolean = true) {
        if(logSummary) {
            logSimulationSummary()
        }
        write()
    }


}
