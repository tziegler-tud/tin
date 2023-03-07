package tin.model.productAutomaton

/**
 * ProductAutomatonEdges here have the following form
 * (source) -[incomingString/outgoingString/cost]-> (target)
 *
 * @param source         the source node (a ProductAutomatonSpecification.ProductAutomatonNode)
 * @param target         the target node (a ProductAutomatonSpecification.ProductAutomatonNode)
 * @param incomingString the string we (as a transducer) read
 * @param outgoingString the string we (as a transducer) return as a replacement
 * @param cost           the cost of the replacement
 */
class ProductAutomatonEdge (
        val source: ProductAutomatonNode,
        val target: ProductAutomatonNode,
        val incomingString: String,
        val outgoingString: String,
        val cost: Double

) {

    override fun toString(): String {
        val eps = "ε"
        val incoming: String
        val outgoing: String
        incoming = if (incomingString.isEmpty()) {
            eps
        } else incomingString
        outgoing = if (outgoingString.isEmpty()) {
            eps
        } else outgoingString
        return String.format("(%s, %s, %s) -[%3s, %3s, %3s]-> (%s, %s, %s)",
                source.identifier.value0.identifier, source.identifier.value1.identifier, source.identifier.value2.identifier,
                incoming, outgoing, cost,
                target.identifier.value0.identifier, target.identifier.value1.identifier, target.identifier.value2.identifier
        )
    }

    fun print() {
        println(toString())
    }

    fun equals(otherEdge: ProductAutomatonEdge): Boolean {
        return source.equalsExcludingEdges(otherEdge.source) &&
                target.equalsExcludingEdges(otherEdge.target) && incomingString == otherEdge.incomingString && outgoingString == otherEdge.outgoingString && cost == otherEdge.cost
    }
}