package policies.mcts.node

interface NodeValueFn: (MCTSChildNode, Double) -> Double