package tin.model.v2.genericGraph

import tin.model.v1.alphabet.Alphabet
import tin.model.v2.graph.*
import tin.model.v2.query.QueryEdgeSet

class GenericGraph : AbstractGraph() {
    override var nodes: NodeSet = NodeSet()
    override var edges = GenericEdgeSet();
    override var alphabet: Alphabet = Alphabet();


    override fun addEdge(edge: Edge) : Boolean {
        /**
         * add nodes if not present
         */
        if (nodes.contains(edge.source)) {
            nodes.add(edge.source)
        }

        if (nodes.contains(edge.target)) {
            nodes.add(edge.target)
        }
        return edges.add(edge.asGenericEdge()!!);
    }


}