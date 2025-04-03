package tinDL.model.v1.transducer

import tinDL.model.v1.graph.Edge

class TransducerEdge(
    override var source: TransducerNode,
    override var target: TransducerNode,
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
        return source.equalsWithoutEdges(other.source) &&
                target.equalsWithoutEdges(other.target)
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