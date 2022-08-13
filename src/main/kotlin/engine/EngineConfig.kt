package engine

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.io.File

@Serializable
data class EngineConfig(
    @SerialName("log_directory") val logDirectory: String,
) {
}