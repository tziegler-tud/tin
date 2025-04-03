package tinDL.services.internal.dijkstra.algorithms

import tinDL.model.v1.utils.ProductAutomatonTuple
import tinDL.model.v1.productAutomaton.ProductAutomatonEdge
import tinDL.model.v1.productAutomaton.ProductAutomatonGraph
import tinDL.model.v1.productAutomaton.ProductAutomatonNode


object DijkstraAlgorithmUtils {
    fun retrieveResultForOneInitialNode(
        initialNode: ProductAutomatonNode,
        setOfNodes: Set<ProductAutomatonNode>
    ): HashMap<ProductAutomatonTuple, Double> {
        val answerMap = HashMap<ProductAutomatonTuple, Double>()

        // for all nodes in set S
        for (node in setOfNodes) {
            // if you are a final state and your weight is not infinite (that means there is a path from the source to you)
            if (node.isFinalState && !node.weight.isInfinite()) {
                // I add you to the final answerSet in the form of ((source, target), weight)
                val answerPair = ProductAutomatonTuple(initialNode, node)
                answerMap[answerPair] = node.weight
            }
        }
        return answerMap
    }

    fun relax(
        u: ProductAutomatonNode,
        v: ProductAutomatonNode,
        edge: ProductAutomatonEdge,
        predecessor: HashSet<ProductAutomatonTuple>
    ) {
        val newCost = u.weight + edge.cost

        // line 8.1
        if (v.weight >= newCost && !newCost.isInfinite()) {

            // line 8.2
            v.weight = newCost

            // line 8.3
            // predecessor of v is u.
            // find pair where v is target and set u the source, if no element -> add a new one.
            predecessor.firstOrNull { it.targetProductAutomatonNode == v }
                ?.let { existingTuple -> existingTuple.sourceProductAutomatonNode = u }
                ?: predecessor.add(ProductAutomatonTuple(u, v))
        }
    }


    fun initialiseSingleSource(
        graph: ProductAutomatonGraph,
        sourceNode: ProductAutomatonNode,
        predecessor: HashSet<ProductAutomatonTuple>
    ) {

        // line 1.1
        for (node in graph.nodes) {

            // line 1.2
            node.weight = Double.POSITIVE_INFINITY

            // line 1.3
            predecessor.forEach { tuple ->
                if (tuple.targetProductAutomatonNode == node) {
                    tuple.sourceProductAutomatonNode = null
                }
            }
        }
        // line 1.4
        sourceNode.weight = 0.0
    }
}
