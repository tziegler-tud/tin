package tin.model.productAutomaton

import tin.model.transducer.TransducerNode
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
            if (node.identifier.first.identifier.equals(
                    productAutomatonNode.identifier.first.identifier,
                    ignoreCase = true
                ) &&
                node.identifier.second.identifier.equals(
                    productAutomatonNode.identifier.second.identifier,
                    ignoreCase = true
                ) &&
                node.identifier.third.identifier.equals(
                    productAutomatonNode.identifier.third.identifier,
                    ignoreCase = true
                )
            ) return
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
     *
     *
     * Note that we cannot use "nodes.add()" or similar functions that use "equals()" here.
     * This is because we call this function to populate the graph;
     * i.e. when calling this function several times after another
     * even the same node differs now from the last iteration because one edge was added.
     * We thus have to treat the comparison of nodes differently for the sake of keeping it unique outside the graph population.
     *
     */
    fun addProductAutomatonEdge(
        source: ProductAutomatonNode,
        target: ProductAutomatonNode,
        incoming: String,
        outgoing: String,
        cost: Double
    ) {

        /** check for existing source and target */
        val existingSource = findNodeWithoutEdgeComparison(source)
        val existingTarget = findNodeWithoutEdgeComparison(target)

        /** if it wasn't found: we add the new nodes.*/
        if (existingSource == null) {
            addProductAutomatonNode(source)
        }

        if (existingTarget == null) {
            addProductAutomatonNode(target)
        }

        val newEdge = ProductAutomatonEdge(source, target, incoming, outgoing, cost)

        // don't add duplicate edges!
        for (existingEdge in source.edges) {
            if (existingEdge == newEdge) {
                return
            }
        }
        source.edges.add(newEdge)
    }

    /** helper function to use in the context of populating the graph only.
     * Note that since we cannot have different nodes with the same identifier,
     * we just have to check the identifier property and no other properties.
     */
    private fun findNodeWithoutEdgeComparison(productAutomatonNode: ProductAutomatonNode): ProductAutomatonNode? {
        return nodes.find {
            it.identifier == productAutomatonNode.identifier
        }
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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ProductAutomatonGraph) return false

        return nodes == other.nodes &&
                initialNodes == other.initialNodes &&
                finalNodes == other.finalNodes
    }

    override fun hashCode(): Int {
        var result = nodes.hashCode()
        result = 31 * result + initialNodes.hashCode()
        result = 31 * result + finalNodes.hashCode()
        return result
    }

}
