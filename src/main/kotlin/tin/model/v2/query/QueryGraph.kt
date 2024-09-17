package tin.model.v2.query

import tin.model.v1.alphabet.Alphabet
import tin.model.v2.graph.*
import tin.model.v2.transducer.TransducerEdge

class QueryGraph : AbstractGraph() {
    override var nodes: NodeSet = NodeSet()
    override var edges = QueryEdgeSet()
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
        return edges.add(edge.asQueryEdge()!!);
    }

    fun addEdge(source: Node, target: Node, label: QueryEdgeLabel) : Boolean {
        return addEdge(QueryEdge(source, target, label));
    }
    fun addEdge(source: Node, target: Node, stringLabel: String) : Boolean {
        return addEdge(QueryEdge(source, target, stringLabel));
    }

    override fun containsEdge(edge: Edge) : Boolean {
        val e = edge.asQueryEdge() ?: return false;
        return edges.contains(e)
    }

    override fun getEdgesWithSource(source: Node): List<QueryEdge> {
        return edges.filterForSource(source);
    }

    override fun getEdgesWithTarget(target: Node): List<QueryEdge> {
        return edges.filterForTarget(target);
    }

    override fun getEdgesWithSourceAndTarget(source: Node, target: Node): List<QueryEdge> {
        return edges.filterForSourceAndTarget(source, target);
    }

    override fun getEdgesWithLabel(label: EdgeLabel): List<QueryEdge> {
        return edges.filterForLabel(label);
    }

    override fun printGraph() {
        for (node in nodes) {
            for (edge in edges) {
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