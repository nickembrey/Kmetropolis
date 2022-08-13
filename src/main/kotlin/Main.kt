import com.akuleshov7.ktoml.Toml
import engine.EngineConfig
import engine.GameState
import kingdoms.jansenTollisenBoard
import kotlinx.serialization.decodeFromString
import policies.mcts.DefaultMCTSPolicy
import policies.playout.GreenRolloutPolicy
import policies.playout.score.WeightVpMorePlayoutScoreFn
import policies.provincial.JansenTollisenBoardProvinicial32Policy
import stats.DominionLogger
import java.io.File

const val CONFIG_PATH = "./config/config.toml"

val config = Toml.decodeFromString<EngineConfig>(
    File(CONFIG_PATH).readText()
)
val logger = DominionLogger(config)

fun main() {

    val policies = listOf(// TODO: pick either "rollouts" or "playouts"
        DefaultMCTSPolicy(
            cParameter = 1.0,
            rollouts = 1000,
            GreenRolloutPolicy(),
            WeightVpMorePlayoutScoreFn,
            false),
        JansenTollisenBoardProvinicial32Policy()
    ) // TODO: allow timed rollouts

//    val policies = listOf(
//        DevelopmentPolicy(),
//        GreenRolloutPolicy()
//    )

    val totalGames = 10
    val games: MutableList<GameState> = mutableListOf()

//    getResourceStats(File("/Users/nick/dev/dominion/KDominion/data/dominion-data1"))

    logger.initSimulation()
    for(i in 1..totalGames) {

        println("Starting game $i")
        println("")
//
//        val policy1 = policies.random()
//        var policy2 = policies.random()
//        while(policy1.name == policy2.name) {
//            println("Policies are the same...")
//            println("...re-rolling second policy...")
//            println("")
//            policy2 = policies.random()
//        }

        val gameState = GameState.new(
            policies = policies,
            board = jansenTollisenBoard,
            maxTurns = 999,
            log = true
        )
        logger.initGame(gameState)
        while(!gameState.gameOver) {
            gameState.processNextBranch()
        }

        policies[0].endGame()
        policies[1].endGame()
        logger.recordGame(gameState)

        games.add(gameState)
    }

    // TODO:
    policies[0].shutdown()
    policies[1].shutdown()
    logger.recordSimulation()
}