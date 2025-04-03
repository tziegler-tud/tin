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
class ProductAutomatonEdge(
    val source: ProductAutomatonNode,
    val target: ProductAutomatonNode,
    val incomingString: String,
    val outgoingString: String,
    val cost: Double

) {

    override fun toString(): String {
        val eps = "epsilon"
        val incoming: String = incomingString.ifEmpty {
            eps
        }
        val outgoing: String = outgoingString.ifEmpty {
            eps
        }
        return String.format(
            "(%s, %s, %s) -[%3s, %3s, %3s]-> (%s, %s, %s)",
            source.identifier.first.identifier, source.identifier.second.identifier, source.identifier.third.identifier,
            incoming, outgoing, cost,
            target.identifier.first.identifier, target.identifier.second.identifier, target.identifier.third.identifier
        )
    }

    fun print() {
        println(toString())
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ProductAutomatonEdge) return false

        return source.equalsWithoutEdges(other.source) &&
                target.equalsWithoutEdges(other.target) &&
                incomingString == other.incomingString &&
                outgoingString == other.outgoingString &&
                cost == other.cost
    }

    override fun hashCode(): Int {
        var result = source.identifierString.hashCode()
        result = 31 * result + target.identifierString.hashCode()
        result = 31 * result + incomingString.hashCode()
        result = 31 * result + outgoingString.hashCode()
        result = 31 * result + cost.hashCode()
        return result
    }
}