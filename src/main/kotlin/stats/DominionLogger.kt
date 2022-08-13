package stats

import engine.EngineConfig
import engine.GameResult
import engine.GameState
import engine.branch.BranchContext
import engine.card.Card
import engine.player.Player
import engine.player.PlayerNumber
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import stats.binning.GameStage
import util.SimulationTimer
import java.io.File
import kotlin.collections.ArrayList


// TODO: add current commit? and state?

// TODO: it would be cool if we could have a setting to make the logs look like dominion.games logs

class DominionLogger(config: EngineConfig) {

    private val logFile: File

    private val gameVpRecords: MutableMap<String, Int> = mutableMapOf()
    private val totalVpRecords: MutableMap<String, Int> = mutableMapOf()
    private val winRecords: MutableMap<String, Int> = mutableMapOf()
    private val tieRecords: MutableMap<Pair<String, String>, Int> = mutableMapOf()
    private val playRecords: MutableMap<String, Int> = mutableMapOf()

    private var gameWeightsUsed: Int = 0
    private var totalWeightsUsed: Int = 0

    private var decisionMaxTreeDepths: MutableList<Int> = mutableListOf()
    private var decisionMaxTreeTurns: MutableList<Int> = mutableListOf()

    private val contextMap: MutableMap<BranchContext, Long> = mutableMapOf()

    private val gameDeckCardCompositions:
            MutableMap<PlayerNumber, MutableList<Pair<GameStage, Map<Card, Double>>>> = mutableMapOf()
    private val savedDeckCardCompositions:
            MutableList<Triple<GameStage, Map<Card, Double>, GameResult>> = mutableListOf()

    fun addDeckCardComposition(
        playerNumber: PlayerNumber,
        gameStage: GameStage,
        map: Map<Card, Double>
    ) {
        val list = gameDeckCardCompositions[playerNumber]
        gameDeckCardCompositions[playerNumber] = if(list != null) {
            list.add(Pair(gameStage, map))
            list
        } else {
            ArrayList(listOf(Pair(gameStage, map))).apply { ensureCapacity(100) }
        }
    }

    init {

        // TODO: what the fuck is this
        val serializationProperty = System.getProperty("kotlinx.serialization.json.pool.size")
        if(serializationProperty == null) {
            System.setProperty("kotlinx.serialization.json.pool.size", (1024 * 1024).toString())
        }

        val base = "dominion-log"
        var number = 0
        var candidateFile = File(config.logDirectory, base + number)
        while(candidateFile.exists()) {
            number += 1
            candidateFile = File(config.logDirectory, base + number)
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

    fun appendLine(str: String = "") {
        log.appendLine(str)
    }

    private fun write() {
        logFile.printWriter().use {
            it.print(log)
        }
    }


    fun initPlayout() {}

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

    fun logWeightUse() {
        gameWeightsUsed += 1
        totalWeightsUsed += 1
    }

    fun initGame(state: GameState) {
        gameVpRecords[state.players[0].name] = 0
        gameVpRecords[state.players[1].name] = 0
        totalVpRecords.putIfAbsent(state.players[0].name, 0)
        totalVpRecords.putIfAbsent(state.players[1].name, 0)
        winRecords.putIfAbsent(state.players[0].name, 0)
        winRecords.putIfAbsent(state.players[1].name, 0)
        playRecords.putIfAbsent(state.players[0].name, 0)
        playRecords.putIfAbsent(state.players[1].name, 0)
        val tiePlayers = state.players.sortedBy { it.name }.map { it.name }
        tieRecords.putIfAbsent(Pair(tiePlayers[0], tiePlayers[1]), 0)
        gameWeightsUsed = 0
    }

    // TODO: have all the intermediate logging be done from the operationHistory, not the code itself
    fun recordGame(
        state: GameState,
        logSummary: Boolean = true
    ) {

        val playerOne = state.players[0]
        val playerTwo = state.players[1]
        val playerOneName = playerOne.name
        val playerTwoName = playerTwo.name
        val tiePlayers = state.players.sortedBy { it.name }.map { it.name }

        gameVpRecords.merge(playerOneName, playerOne.vp, Int::plus)
        gameVpRecords.merge(playerTwoName, playerTwo.vp, Int::plus)
        totalVpRecords.merge(playerOneName, playerOne.vp, Int::plus)
        totalVpRecords.merge(playerTwoName, playerTwo.vp, Int::plus)

        if(playerOne.vp > playerTwo.vp) {
            winRecords.merge(playerOneName, 1, Int::plus)
            gameDeckCardCompositions[PlayerNumber.PLAYER_ONE]!!
                .map { Triple(it.first, it.second, GameResult.WIN) }
                .forEach { savedDeckCardCompositions.add(it) }
            gameDeckCardCompositions[PlayerNumber.PLAYER_TWO]!!
                .map { Triple(it.first, it.second, GameResult.LOSE) }
                .forEach { savedDeckCardCompositions.add(it) }
        } else if(playerTwo.vp > playerOne.vp) {
            winRecords.merge(playerTwoName, 1, Int::plus)
            gameDeckCardCompositions[PlayerNumber.PLAYER_ONE]!!
                .map { Triple(it.first, it.second, GameResult.LOSE) }
                .forEach { savedDeckCardCompositions.add(it) }
            gameDeckCardCompositions[PlayerNumber.PLAYER_TWO]!!
                .map { Triple(it.first, it.second, GameResult.WIN) }
                .forEach { savedDeckCardCompositions.add(it) }
        } else {
            tieRecords.merge(Pair(tiePlayers[0], tiePlayers[1]), 1, Int::plus)
        }

        gameDeckCardCompositions[PlayerNumber.PLAYER_ONE]!!.clear()
        gameDeckCardCompositions[PlayerNumber.PLAYER_TWO]!!.clear()

        playRecords.merge(playerOneName, 1, Int::plus)
        playRecords.merge(playerTwoName, 1, Int::plus)

        if(logSummary) {
            logGameSummary(players = Pair(playerOne, playerTwo))
        } else {
            log.clear()
        }

        totalGames += 1
        gameVpRecords.remove(playerOne.name)
        gameVpRecords.remove(playerTwo.name)
        gamePlayouts = 0
        gameDecisions = 0
        gameWeightsUsed = 0
    }

    private fun logGameSummary(
        players: Pair<Player, Player>
    ) {
        appendLine("")
        appendLine("------------")
        appendLine("Game summary")
        appendLine("------------")
        appendLine("")
        appendLine("Player 1: ${players.first.policy.name}")
        appendLine("Deck: ")
        for(entry in players.first.allCards.groupingBy { it }.eachCount()) {
            appendLine("      ${entry.key}: ${entry.value}")
        }
        appendLine("")
        appendLine("Player 2: ${players.second.policy.name}")
        appendLine("Deck: ")
        for(entry in players.second.allCards.groupingBy { it }.eachCount()) {
            appendLine("      ${entry.key}: ${entry.value}")
        }
        appendLine("")
        for((policy, vp) in gameVpRecords) {
            appendLine("$policy VP: $vp")
        }
        appendLine("")
        appendLine("Weights used: $gameWeightsUsed times")
        appendLine("Weights used ${gameWeightsUsed.toDouble() / gameDecisions.toDouble()} times per decision")
    }

    fun initSimulation() {}

    fun recordSimulation(
        logSummary: Boolean = true,
        write: Boolean = true
    ) {

        if(logSummary) {
            logSimulationSummary()
        }
        if(write) {
            write()
        }
    }

    private fun logSimulationSummary() {
        appendLine("------------------")
        appendLine("Simulation summary")
        appendLine("------------------")
        appendLine("")
        for((policy, games) in playRecords.toSortedMap()) {
            appendLine("$policy games: $games")
        }
        appendLine("")
        for((policy, vp) in totalVpRecords.toSortedMap()) {
            appendLine("$policy VP: $vp")
        }
        appendLine("")
        for((policy, wins) in winRecords.toSortedMap()) {
            appendLine("$policy wins: $wins")
        }
        appendLine("")
        for((policy, wins) in winRecords.toSortedMap()) {
            appendLine("$policy win percentage: ${wins.toDouble() / playRecords[policy]!!.toDouble() * 100.0}%")
        }
        appendLine("")
        for((pair, ties) in tieRecords.toSortedMap(compareBy { it.first })) {
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
    }


}
