package tinDL.model.v2.genericGraph

import tinDL.model.v1.alphabet.Alphabet
import tinDL.model.v2.graph.*
import tinDL.model.v2.query.QueryEdgeSet

class GenericGraph : tinDL.model.v2.graph.AbstractGraph() {
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