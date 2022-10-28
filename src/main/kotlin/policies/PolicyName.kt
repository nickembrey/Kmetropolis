package policies

@JvmInline
value class PolicyName(val value: String) {
    override fun toString(): String {
        return value
    }
}