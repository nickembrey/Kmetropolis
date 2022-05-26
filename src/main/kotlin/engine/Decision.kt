package engine

data class Decision(
    val choice: Choice,
    val choiceContext: ChoiceContext,
    val index: Int?
)