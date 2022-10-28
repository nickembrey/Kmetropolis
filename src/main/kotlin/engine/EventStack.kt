package engine

class EventStack(
    private val list: MutableList<GameEvent> = mutableListOf()
) {

    fun push(element: GameEvent): Boolean = list.add(element)
    fun pushAll(elements: List<GameEvent>): Boolean = list.addAll(elements)
    fun pop(): GameEvent = list.removeLast()
    fun peek(): GameEvent? = list.lastOrNull()
    fun copy(): EventStack = EventStack(ArrayList(list))
}



