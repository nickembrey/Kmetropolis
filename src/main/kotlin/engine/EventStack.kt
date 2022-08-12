package engine

import engine.branch.BranchContext

class EventStack private constructor(
    private val list: ArrayList<GameEvent> // TODO: try linked list?
) {

    constructor() : this(ArrayList<GameEvent>(500).also {
        it.addAll(GamePhase.NOT_STARTED.events)
    })

    val size = list.size

    fun push(element: GameEvent) {
        list.add(element)
    }
    fun pushAll(elements: List<GameEvent>) = list.addAll(elements)
    fun poll() = list.removeLastOrNull()
    fun pop() = list.removeLast()
    fun peek() = list.lastOrNull()
    fun copy(): EventStack = EventStack(ArrayList(list).apply { ensureCapacity(500) })
}



