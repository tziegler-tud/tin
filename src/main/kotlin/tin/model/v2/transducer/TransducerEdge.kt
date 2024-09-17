package tin.model.v2.transducer

import tin.model.v2.graph.Edge
import tin.model.v2.graph.Node
import tin.model.v2.query.QueryEdge

class TransducerEdge(
    override val source: Node,
    override val target: Node,
    override val label: TransducerEdgeLabel,
) : Edge
{
    constructor(source: Node, target: Node, incoming: String, outgoing: String, cost: Int) : this(source, target, TransducerEdgeLabel(incoming, outgoing, cost))

    override fun toString(): String {
        return "(${source.identifier})-[${label}]-${target.identifier}";
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TransducerEdge) return false

        return checkForNodesEquality(other) &&
                label == other.label
    }
    /**
     * call the trimmed TransducerNode.equals() method in order to prevent a circular dependency.
     */
    private fun checkForNodesEquality(other: TransducerEdge): Boolean {
        return source == other.source &&
                target == other.target
    }

    override fun asTransducerEdge(): TransducerEdge {
        return this;
    }

    override fun asQueryEdge(): QueryEdge? {
        return null;
    }

    override fun hashCode(): Int {
        var result = source.identifier.hashCode()
        result = 31 * result + target.identifier.hashCode()
        result = 31 * result + label.hashCode()
        return result
    }

    override fun print(){
        println(this);
    }
}