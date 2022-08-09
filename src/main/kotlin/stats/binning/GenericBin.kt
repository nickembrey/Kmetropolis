package stats.binning

enum class GenericBin {
    VERY_LOW, LOW, MID, HIGH, VERY_HIGH;

    val next: GenericBin
        get() = when(this) {
            VERY_LOW -> LOW
            LOW -> MID
            MID -> HIGH
            HIGH -> VERY_HIGH
            VERY_HIGH -> VERY_HIGH
        }
}