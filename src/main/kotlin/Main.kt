import com.akuleshov7.ktoml.Toml
import engine.EngineConfig
import experiments.Experiment
import kotlinx.serialization.decodeFromString
import stats.DominionLogger
import stats.LogFormat
import java.io.File

const val CONFIG_PATH = "./config/config.toml"

val config = Toml.decodeFromString<EngineConfig>(
    File(CONFIG_PATH).readText()
)
val logger = DominionLogger(config)

fun main() {
    val experimentResult = Experiment.EASY_EXPERIMENT_1K.run(10)
    logger.logExperimentResult(
        experimentResult = experimentResult,
        logFormat = LogFormat.DEFAULT)
}