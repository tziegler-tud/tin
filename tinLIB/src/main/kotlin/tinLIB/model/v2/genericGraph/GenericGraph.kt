package tinLIB.model.v2.genericGraph

import tinLIB.model.v2.alphabet.Alphabet
import tinLIB.model.v2.graph.*
import tinLIB.model.v2.query.QueryEdgeSet

class GenericGraph : AbstractGraph<Node, GenericEdge>() {
    override var nodes: NodeSet<Node> = NodeSet()
    override var edges = GenericEdgeSet();
    override var alphabet: Alphabet = Alphabet();


    override fun addEdge(edge: GenericEdge) : Boolean {
        /**
         * add nodes if not present
         */
        if (nodes.contains(edge.source)) {
            nodes.add(edge.source)
        }

        if (nodes.contains(edge.target)) {
            nodes.add(edge.target)
        }
        return edges.add(edge);
    }
}