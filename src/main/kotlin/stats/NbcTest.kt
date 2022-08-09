package stats

import engine.GameResult
import engine.player.PlayerProperty
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import org.nield.kotlinstatistics.geometricMean
import org.nield.kotlinstatistics.median
import org.nield.kotlinstatistics.standardDeviation
import org.nield.kotlinstatistics.toNaiveBayesClassifier
import stats.binning.GameStage
import java.io.BufferedReader
import java.io.File

fun getResourceStats(dataFile: File) {

    val data = dataFile.bufferedReader()
    .use { it.readText() }
        .let { Json.decodeFromString<MutableList<Triple<GameStage, Map<PlayerProperty, Double>, GameResult>>>(it) }
        .flatMap { it.second.entries }

//    val actions = data.filter { it.key == PlayerProperty.ACTIONS }.map { it.value }
    val buys = data.filter { it.key == PlayerProperty.BUYS }.map { it.value }
    val coins = data.filter { it.key == PlayerProperty.COINS }.map { it.value }
    val vp = data.filter { it.key == PlayerProperty.BASE_VP }.map { it.value }

//    println("actions min: ${actions.min()}")
//    println("actions max: ${actions.max()}")
//    println("actions median: ${actions.median()}")
//    println("actions mean: ${actions.average()}")
//    println("actions std: ${actions.standardDeviation()}")

    println("buys min: ${buys.min()}")
    println("buys max: ${buys.max()}")
    println("buys median: ${buys.median()}")
    println("buys mean: ${buys.average()}")
    println("buys std: ${buys.standardDeviation()}")

    println("coins min: ${coins.min()}")
    println("coins max: ${coins.max()}")
    println("coins median: ${coins.median()}")
    println("coins mean: ${coins.average()}")
    println("coins std: ${coins.standardDeviation()}")

    println("vp min: ${vp.min()}")
    println("vp max: ${vp.max()}")
    println("vp median: ${vp.median()}")
    println("vp mean: ${vp.average()}")
    println("vp std: ${vp.standardDeviation()}")
}



