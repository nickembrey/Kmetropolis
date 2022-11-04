package stats

import engine.EngineConfig
import engine.GameResult
import engine.GameState
import engine.branch.BranchContext
import experiments.ExperimentResult
import util.SimulationTimer
import java.io.File


// TODO: add current commit? and state?

// TODO: it would be cool if we could have a setting to make the logs look like dominion.games logs

class DominionLogger(config: EngineConfig) {

    private val logFile: File
    private val treeFile: File

    private val playRecords: MutableMap<String, Int> = mutableMapOf()

    private var gameWeightsUsed: Int = 0
    private var totalWeightsUsed: Int = 0

    private var decisionMaxTreeDepths: MutableList<Int> = mutableListOf()
    private var decisionMaxTreeTurns: MutableList<Int> = mutableListOf()

    private var gameTree: List<Pair<Int, Int>> = listOf()

    private val contextMap: MutableMap<BranchContext, Long> = mutableMapOf()

    init {

        // TODO: what the fuck is this
        val serializationProperty = System.getProperty("kotlinx.serialization.json.pool.size")
        if(serializationProperty == null) {
            System.setProperty("kotlinx.serialization.json.pool.size", (1024 * 1024).toString())
        }

        val base = "dominion-log"
        val treeBase = "dominion-tree"
        var number = 0
        var candidateFile = File(config.logDirectory, base + number)
        while(candidateFile.exists()) {
            number += 1
            candidateFile = File(config.logDirectory, base + number)
        }
        logFile = candidateFile
        treeFile = File(config.logDirectory + "/tree", "$treeBase$number.dot")
    }

    private val timer: SimulationTimer = SimulationTimer()

    private var gamePlayouts = 0
    private var gameDecisions = 0

    private var totalPlayouts = 0
    private var totalDecisions = 0
    private var totalGames = 0

    private var log = StringBuilder()
    private var treeLog = StringBuilder()

    fun append(str: String) {
        log.append(str)
    }

    private fun appendLine(str: String = "") {
        log.appendLine(str)
    }

    private fun write() {
        logFile.printWriter().use {
            it.print(log)
        }
    }

    private fun writeTree() {
        treeFile.printWriter().use {
            it.print(treeLog)
        }
    }

    fun recordPlayout() {
        gamePlayouts += 1
        totalPlayouts += 1
    }
    fun initDecision() = timer.start()

    fun recordDecision(decisionContextMap: Map<BranchContext, Int>, maxDepth: Int, maxTurns: Int) {
        timer.stop()
        gameDecisions += 1
        totalDecisions += 1
        decisionMaxTreeDepths.add(maxDepth)
        decisionMaxTreeTurns.add(maxTurns)
        decisionContextMap.forEach {
            contextMap.merge(it.key, it.value.toLong(), Long::plus)
        }
    }

    fun initGame(state: GameState) { // TODO: roll this into the results
        playRecords.putIfAbsent(state.players[0].name, 0)
        playRecords.putIfAbsent(state.players[1].name, 0)
    }

    // TODO: have all the intermediate logging be done from the operationHistory, not the code itself
    fun recordGame(
        state: GameState
    ) {

        val playerOne = state.players[0]
        val playerTwo = state.players[1]
        val playerOneName = playerOne.name
        val playerTwoName = playerTwo.name

        playRecords.merge(playerOneName, 1, Int::plus)
        playRecords.merge(playerTwoName, 1, Int::plus)

        totalGames += 1
        gamePlayouts = 0
        gameDecisions = 0
        gameWeightsUsed = 0
    }

    fun logTree(tree: List<Pair<Int, Int>>) {
        treeLog.appendLine("digraph {")
        tree.forEach {
            treeLog.appendLine(
                "${it.first} -> ${it.second};"
            )
        }
        treeLog.appendLine("}")
        writeTree()
    }

    fun logExperimentResult(
        experimentResult: ExperimentResult,
        logFormat: LogFormat
    ) {

        for(i in experimentResult.gameLogs.indices) {
            appendLine("------------------")
            appendLine("Game ${i + 1}")
            appendLine("------------------")
            appendLine("")
            for(selection in experimentResult.gameLogs[i]) {
                appendLine("$selection")
            }
            for(entry in experimentResult.gameResults) {
                appendLine("")
                appendLine("${entry.value[i].playerNumber}: ${entry.key}")
                appendLine("Deck: ")
                for(card in entry.value[i].deck.groupingBy { it }.eachCount()) {
                    appendLine("      ${card.key}: ${card.value}")
                }
            }
            appendLine("")
        }

        // TODO: move all of this stuff into result
        appendLine("------------------")
        appendLine("Simulation summary")
        appendLine("------------------")
        appendLine("")
        for((policy, games) in playRecords.toSortedMap()) {
            appendLine("$policy games: $games")
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
        appendLine("")
        appendLine("Weights used: $totalWeightsUsed times")
        appendLine("Weights used ${totalWeightsUsed.toDouble() / totalDecisions.toDouble()} times per decision")
        appendLine("")
        appendLine("Average max tree depth: ${decisionMaxTreeDepths.average()}")
        appendLine("Average max tree turns: ${decisionMaxTreeTurns.average()}")
        if(totalPlayouts > 0 && totalDecisions > 0) {
            appendLine("Depth per playout: ${decisionMaxTreeDepths.average() / (totalPlayouts / totalDecisions)}")
            appendLine("Turns per playout: ${decisionMaxTreeTurns.average() / (totalPlayouts / totalDecisions)}")
        }
        appendLine("")
        appendLine("Context distribution: ")
        val allContexts = contextMap.values.sum()
        for(entry in contextMap) {
            appendLine("      ${entry.key}: ${entry.value.toDouble() * 100 / allContexts }% (${entry.value})")
        }
        appendLine("")

        for(entry in experimentResult.gameResults) {
            appendLine("Player: ${entry.key}")
            appendLine("Wins: ${entry.value.map { it.result }.count { it == GameResult.WIN }} ")
            appendLine("VP: ${entry.value.sumOf { it.vp }} ")
            appendLine("")
        }

        write()
    }


}
