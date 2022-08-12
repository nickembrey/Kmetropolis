package ml

import engine.GameResult
import engine.GameState
import engine.branch.BranchContext
import engine.branch.BranchSelection
import engine.card.Card
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.nield.kotlinstatistics.toNaiveBayesClassifier
import stats.binning.Bins.getGameStage
import stats.binning.Bins.getResourceBin
import stats.binning.GameStage
import stats.binning.GenericBin
import java.io.File

class NaiveBayesResourceCompositionWeightSource: WeightSource {

    private val data: MutableList<Triple<GameStage, Map<Card, Double>, GameResult>> = mutableListOf()

    init {
        val dir = "/Users/nick/dev/dominion/KDominion/data/"
        val base = "dominion-data"
        var number = 0
        var candidateFile = File(dir, base + number)
        while(candidateFile.exists()) {
            candidateFile.bufferedReader()
                .use { it.readText() }
                .let { Json.decodeFromString<MutableList<Triple<GameStage, Map<Card, Double>, GameResult>>>(it) }
                .let { data.addAll(it) }
            number += 1
            candidateFile = File(dir, base + number)
        }
    }


    private val classifier = data.toNaiveBayesClassifier(
        featuresSelector = {
                it.second.map { entry -> Triple(entry.key, it.first, getResourceBin(entry.value)) }
        },
        categorySelector = { it.third }
    )

    override fun getWeights(state: GameState, selections: Collection<BranchSelection>): List<Double> {
        if(state.context != BranchContext.CHOOSE_BUYS) {
            throw IllegalStateException()
        }
        val currentGameStage = getGameStage(state.board)
        val currentResources: Map<Card, Int> = state.currentPlayer.toCardFrequencyMap(state.board)
        val currentFeatures: List<Triple<Card, GameStage, GenericBin>> = currentResources.map { entry -> Triple(entry.key, currentGameStage, getResourceBin(entry.value.toDouble() / state.currentPlayer.cardCount.toDouble())) }
        val currentPrediction = classifier.predictWithProbability(currentFeatures)
        return selections.map { selection ->
            if(selection is Card) {
                val newFeatures: List<Triple<Card, GameStage, GenericBin>> = currentFeatures.map {
                    if(it.first == selection) {
                        Triple(it.first, it.second, it.third.next)
                    } else {
                        it
                    }
                }
                val newPrediction = classifier.predictWithProbability(newFeatures)
                if(currentPrediction == null || newPrediction == null) {
                    1.0 // TODO: what does this even mean?
                } else {
                    when(newPrediction.category) {
                        GameResult.WIN -> when(currentPrediction.category) {
                            GameResult.WIN -> 1.0
                            GameResult.LOSE -> 2.0
                            else -> throw IllegalStateException()
                        }
                        GameResult.LOSE -> when(currentPrediction.category) {
                            GameResult.WIN -> 0.5
                            GameResult.LOSE -> 1.0
                            else -> throw IllegalStateException()
                        }
                        else -> throw IllegalStateException()
                    }
                }

            } else {
                1.0
            }
        }
    }
}