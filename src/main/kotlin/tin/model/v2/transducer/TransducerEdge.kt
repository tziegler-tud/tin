package tin.model.v2.transducer

import tin.model.v2.graph.Edge
import tin.model.v2.graph.Node

class TransducerEdge(
    source: Node,
    target: Node,
    label: TransducerEdgeLabel,

) : Edge(
        source, target, label
){

    override fun toString(): String {
        return "(${source.identifier})-["
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

    override fun hashCode(): Int {
        var result = source.identifier.hashCode()
        result = 31 * result + target.identifier.hashCode()
        result = 31 * result + label.hashCode()
        return result
    }
}