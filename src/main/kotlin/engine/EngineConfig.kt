package engine

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EngineConfig(
    @SerialName("log_directory") val logDirectory: String,
)