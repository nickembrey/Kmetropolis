package policies

class DefaultMCTSPolicy(
    cParameter: Double,
    rollouts: Int
): MCTSPolicy(
    cParameter = cParameter,
    rollouts = rollouts
) {
    override val name = PolicyName("defaultMCTSPolicy")

    override val executor = CurrentThreadExecutor()

    override fun shutdown() {}
}