package mcts

class MCTSTreeNode(val parent: MCTSTreeNode? = null, val decision: Int? = null) {
    var wins: Int = 0
    var simulations: Int = 0

    var children: MutableList<MCTSTreeNode> = mutableListOf()

    fun addChild(decision: Int) {
        children.add(MCTSTreeNode(this, decision))
    }
}