package engine

interface CardChoices { // TODO: add getting card function here
    val choices: List<*> // TODO: I wonder if this could be any better...
    fun isEmpty(): Boolean = choices.isEmpty()
    fun isNotEmpty(): Boolean = choices.isNotEmpty()
}

// TODO: note that null is used for picking no card

@JvmInline
value class SingleCardChoices(override val choices: List<Card?> = listOf()): CardChoices

@JvmInline
value class MultipleCardChoices(override val choices: List<List<Card>> = listOf()): CardChoices
// TODO: consider changing to a set
// TODO: make sure you can always choose no card or a list of empty cards