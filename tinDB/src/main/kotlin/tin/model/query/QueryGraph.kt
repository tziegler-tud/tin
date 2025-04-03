package tin.model.query

import tin.model.database.DatabaseNode
import tin.model.graph.Graph
import tin.model.graph.Node
import tin.model.graph.NodeSet
import tin.model.transducer.TransducerNode

class QueryGraph : Graph() {
    override var nodes: NodeSet<QueryNode> = NodeSet()

    fun addNodes(vararg n: QueryNode){
        nodes.addAll(listOf(*n))
    }

    override fun getNode(identifier: String) : QueryNode? {
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
    fun addEdge(source: QueryNode, target: QueryNode, label: String) {
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

        val newEdge = QueryEdge(source, target, label)

        // don't add duplicate edges!
        for (existingEdge in source.edges) {
            if (existingEdge == newEdge) {
                return
            }
        }
        source.edges.add(newEdge)
    }

    override fun printGraph() {
        for (node in nodes) {
            for (edge in node.edges) {
                edge.print()
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is QueryGraph) return false

        return super.equals(other);

//        return nodes == other.nodes &&
//                alphabet == other.alphabet
    }

    override fun hashCode(): Int {
        var result = nodes.hashCode()
        result = 31 * result + alphabet.hashCode()
        return result
    }


}