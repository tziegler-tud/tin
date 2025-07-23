package tinDB.model.v2.productAutomaton

import tinLIB.model.v2.graph.AbstractEdge

/**
 * ProductAutomatonEdges here have the following form
 * (source) -[incomingString/outgoingString/cost]-> (target)
 *
 * @param source         the source node (a ProductAutomatonSpecification.ProductAutomatonNode)
 * @param target         the target node (a ProductAutomatonSpecification.ProductAutomatonNode)
 * @param label          the edge label consisting of [incoming, outgoing, cost]
 */
class ProductAutomatonEdge(
    override val source: ProductAutomatonNode,
    override val target: ProductAutomatonNode,
    override val label: ProductAutomatonEdgeLabel,

) : AbstractEdge(
    source,
    target,
    label
){

    override fun toString(): String {
        val eps = "epsilon"
        val incoming: String = if(label.incoming.isEpsilonLabel()) eps else label.incoming.getLabel()
        val outgoing: String = if(label.outgoing.isEpsilonLabel()) eps else label.outgoing.getLabel()

        return String.format(
            "%s -[%3s, %3s, %3s]-> %s",
            source.identifier,
            label.toString(),
            target.identifier
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ProductAutomatonEdge) return false

        return source == other.source &&
                target == other.target &&
                label == other.label
    }

    override fun hashCode(): Int {
        var result = source.hashCode()
        result = 31 * result + target.hashCode()
        result = 31 * result + label.hashCode()
        return result
    }
}