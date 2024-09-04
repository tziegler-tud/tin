package tin.model.v2.transducer

import tin.model.v2.graph.Edge
import tin.model.v2.graph.Node

class TransducerEdge(
    override var source: Node,
    override var target: Node,
    var incomingString: String,
    var outgoingString: String,
    var cost: Double
) : Edge(
        source, target, "$incomingString, $outgoingString, $cost"
){

    override fun toString(): String {
        val eps = "epsilon"
        val incoming: String = if (incomingString.isEmpty()) {
            eps
        } else incomingString
        val outgoing: String = if (outgoingString.isEmpty()) {
            eps
        } else outgoingString
        return String.format(
            "(%s) -[%3s|%3s|%3s]-> (%s)",
            source.identifier,
            incoming,
            outgoing,
            cost,
            target.identifier
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TransducerEdge) return false

        return checkForNodesEquality(other) &&
                incomingString == other.incomingString &&
                outgoingString == other.outgoingString &&
                cost == other.cost
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
        result = 31 * result + incomingString.hashCode()
        result = 31 * result + outgoingString.hashCode()
        result = 31 * result + cost.hashCode()
        return result
    }
}