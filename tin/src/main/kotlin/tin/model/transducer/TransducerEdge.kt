package tin.model.transducer

class TransducerEdge(
        val source: TransducerNode,
        val target: TransducerNode,
        val incomingString: String,
        val outgoingString: String,
        val cost: Double
) {

    override fun toString(): String {
        val eps = "ε"
        val incoming: String = if (incomingString.isEmpty()) {
            eps
        } else incomingString
        val outgoing: String = if (outgoingString.isEmpty()) {
            eps
        } else outgoingString
        return String.format("(%s) -[%3s|%3s|%3s]-> (%s)", source.identifier, incoming, outgoing, cost, target.identifier)
    }

    fun print() {
        println(this)
    }

    fun equals(otherEdge: TransducerEdge): Boolean {
        return source.compareToExcludingEdges(otherEdge.source) &&
                target.compareToExcludingEdges(otherEdge.target) && incomingString == otherEdge.incomingString && outgoingString == otherEdge.outgoingString && cost == otherEdge.cost
    }
}