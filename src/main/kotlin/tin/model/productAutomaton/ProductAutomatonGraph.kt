package tin.model.productAutomaton

import java.util.*

class ProductAutomatonGraph {
    var nodes: MutableSet<ProductAutomatonNode> = HashSet()
    var initialNodes: MutableSet<ProductAutomatonNode> = HashSet()
    var finalNodes: MutableSet<ProductAutomatonNode> = HashSet()

    /**
     * adds one node to the set of nodes (if it is not already contained)
     * adds the node to the set of initial and final states if it has one of these properties.
     *
     * @param productAutomatonNode the productAutomatonNode
     */
    fun addProductAutomatonNode(productAutomatonNode: ProductAutomatonNode) {
        for (node in nodes) {
            if (node.identifier.first.identifier.equals(productAutomatonNode.identifier.first.identifier, ignoreCase = true) &&
                    node.identifier.second.identifier.equals(productAutomatonNode.identifier.second.identifier, ignoreCase = true) &&
                    node.identifier.third.identifier.equals(productAutomatonNode.identifier.third.identifier, ignoreCase = true)) return
        }
        nodes.add(productAutomatonNode)
        if (productAutomatonNode.isInitialState) {
            initialNodes.add(productAutomatonNode)
        }
        if (productAutomatonNode.isFinalState) {
            finalNodes.add(productAutomatonNode)
        }
    }

    /**
     * adds one edge to the corresponding node (if it is not already contained)
     *
     * @param source   the source node (productAutomatonNode)
     * @param target   the target node (productAutomatonNode)
     * @param incoming the incoming string
     * @param outgoing the replacement of the incoming string
     * @param cost     the cost of the replacement operation
     */
    fun addProductAutomatonEdge(source: ProductAutomatonNode, target: ProductAutomatonNode, incoming: String, outgoing: String, cost: Double) {

        // add the nodes of the edge first
        addProductAutomatonNode(source)
        addProductAutomatonNode(target)

        // check whether the edge is already there
        for (edge in source.edges) {
            if ((edge.source.equals(source)) && (edge.target.equals(target)) && edge.incomingString.equals(incoming, ignoreCase = true)
                    && edge.outgoingString.equals(outgoing, ignoreCase = true) && (edge.cost == cost)) {
                return
            }
        }
        // if the edge is new -> add it.
        source.edges.add(ProductAutomatonEdge(source, target, incoming, outgoing, cost))
    }

    /**
     * TODO: refactor print methods into proper .txt file output.
     * prints the graph by going through every node and printing every edge of that node.
     * Use with caution! Bigger graphs might make debugging a little hard.
     */
    fun printGraph() {
        for (node in nodes) {
            for (edge in node.edges) {
                edge.print()
            }
        }
    }

    // only use this for test purposes!
    // contains flaws in the first condition, do not use!
    fun equals(otherGraph: ProductAutomatonGraph): Boolean {
        return if (nodes.size != otherGraph.nodes.size) {
            false
        } else equalsOtherNodeSet(otherGraph.nodes)
    }

    private fun equalsOtherNodeSet(otherNodeSet: Set<ProductAutomatonNode>?): Boolean {
        var pairsFound = 0
        for (thisNode in nodes) {
            for (otherNode in otherNodeSet!!) {
                if (thisNode.equals(otherNode)) {
                    pairsFound++
                    break
                }
            }
        }
        return pairsFound == nodes.size
    }
}
