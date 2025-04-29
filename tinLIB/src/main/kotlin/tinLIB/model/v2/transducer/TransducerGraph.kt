package tinLIB.model.v2.transducer

import tinLIB.model.v2.alphabet.Alphabet
import tinLIB.model.v2.graph.*

class TransducerGraph() : AbstractGraph<Node, TransducerEdge>() {
    override val nodes: NodeSet<Node> = NodeSet()
    override val edges: TransducerEdgeSet = TransducerEdgeSet()

    override var alphabet: Alphabet = Alphabet();

    fun addEdge(source: Node, target: Node, transducerEdgeLabel: TransducerEdgeLabel) : Boolean {
        return addEdge(TransducerEdge(source, target, transducerEdgeLabel))
    }

    fun addEdge(source: Node, target: Node, incoming: String, outgoing: String, cost: Int) : Boolean {
        return addEdge(TransducerEdge(source, target, incoming, outgoing, cost))
    }

    override fun addEdge(edge: TransducerEdge) : Boolean {
        if (!nodes.contains(edge.source)) {
            nodes.add(edge.source)
        }
        if (!nodes.contains(edge.target)) {
            nodes.add(edge.target)
        }
        return edges.add(edge.asTransducerEdge()!!);
    }

    override fun containsEdge(edge: TransducerEdge) : Boolean {
        val e = edge.asTransducerEdge() ?: return false;
        return edges.contains(e)
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

    override fun getEdgesWithLabel(label: EdgeLabel): List<TransducerEdge> {
        return edges.filterForLabel(label);
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TransducerGraph) return false
        return super.equals(other);
    }

    override fun isEmpty(): Boolean {
        return nodes.isEmpty()
    }

    override fun isValidGraph(): Boolean {
        return hasInitialNode() && hasFinalNode()
    }

    override fun hasInitialNode(): Boolean{
        return nodes.any{it.isInitialState}
    }
    override fun hasFinalNode(): Boolean{
        return nodes.any{it.isFinalState}
    }
}
