package tin.model.transducer

import tin.model.database.DatabaseNode
import tin.model.graph.Graph
import tin.model.graph.NodeSet

class TransducerGraph : Graph() {
    override val nodes: NodeSet<TransducerNode> = NodeSet()

    fun addNodes(vararg n: TransducerNode){
        nodes.addAll(listOf(*n))
    }

    override fun getNode(identifier: String) : TransducerNode? {
        return nodes.find {
            it.identifier == identifier
        }
    }

    /**
     * Note that we cannot use "nodes.add()" or similar functions that use "equals()" here.
     * This is because we call this function to populate the graph;
     * i.e. when calling this function several times after another
     * even the same node differs now from the last iteration because one edge was added.
     * We thus have to treat the comparison of nodes differently for the sake of keeping it unique outside the graph population.
     */
    fun addEdge(source: TransducerNode, target: TransducerNode, incoming: String, outgoing: String, cost: Double) {
        /** check for existing source and target */
        val existingSource = findNodeWithoutEdgeComparison(source)
        val existingTarget = findNodeWithoutEdgeComparison(target)

        /** if it wasn't found: we add the new nodes.*/
        if (existingSource == null) {
            nodes.add(source)
        }

        if (existingTarget == null) {
            nodes.add(target)
        }

        // Create the new edge
        val newEdge = TransducerEdge(existingSource?: source, existingTarget?:target, incoming, outgoing, cost)

        // don't add duplicate edges!
        for (existingEdge in source.edges!!) {
            if (existingEdge == newEdge) {
                return
            }
        }
        source.edges.add(newEdge)
    }

    // TODO: print isolated nodes. (same as in the query)
    override fun printGraph() {
        for (node in nodes) {
            for (edge in node.edges) {
                edge.print()
            }
        }
    }

    /** helper function to use in the context of populating the graph only.
     * Note that since we cannot have different nodes with the same identifier,
     * we just have to check the identifier property and no other properties.
     */
    private fun findNodeWithoutEdgeComparison(transducerNode: TransducerNode): TransducerNode? {
        return nodes.find {
            it.identifier == transducerNode.identifier
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TransducerGraph) return false

        return super.equals(other);

//        return nodes == other.nodes
    }

    override fun hashCode(): Int {
        return nodes.hashCode()
    }
}
