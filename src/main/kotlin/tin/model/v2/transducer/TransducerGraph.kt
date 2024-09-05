package tin.model.v2.transducer

import tin.model.v2.graph.Graph
import tin.model.v2.graph.NodeSet
import tin.model.v2.graph.Edge
import tin.model.v2.graph.Node
import tin.model.v2.query.QueryEdge

class TransducerGraph : Graph() {
    override val nodes: NodeSet = NodeSet()
    override val edges: TransducerEdgeSet = TransducerEdgeSet()

    fun addEdge(source: Node, target: Node, incoming: String, outgoing: String, cost: Int) {
        addEdge(TransducerEdge(source, target, incoming, outgoing, cost))
    }

    override fun addEdge(edge: Edge) : Boolean {

        if (!nodes.contains(edge.source)) {
            nodes.add(edge.source)
        }

        if (!nodes.contains(edge.target)) {
            nodes.add(edge.target)
        }
        return edges.add(edge.asTransducerEdge());
    }

    override fun containsEdge(edge: Edge) : Boolean {
        return edges.contains(edge.asTransducerEdge())
    }

    override fun getEdgesWithSource(source: Node): List<TransducerEdge> {
        return edges.filterForSource(source);
    }

    override fun getEdgesWithTarget(target: Node): List<TransducerEdge> {
        return edges.filterForTarget(target);
    }

    override fun getEdgesWithSourceAndTarget(source: Node, target: Node): List<TransducerEdge> {
        return edges.filterForSourceAndTarget(source, target);
    }

    override fun getEdgesWithLabel(label: String): List<TransducerEdge> {
        return edges.filterForLabel(label);
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
