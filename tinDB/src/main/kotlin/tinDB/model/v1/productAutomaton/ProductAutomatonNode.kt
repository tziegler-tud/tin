package tinDB.model.v1.productAutomaton

import tinDB.model.v1.database.DatabaseNode
import tinDB.model.v1.query.QueryNode
import tinDB.model.v1.transducer.TransducerNode
import java.lang.Double.POSITIVE_INFINITY
import kotlin.collections.HashSet

/**
 * constructor for a productAutomatonNode. it has the form
 * (queryNode, transducerNode, databaseNode) and has booleans for being an initial or final state
 *
 * @param queryNode        the corresponding queryNode
 * @param transducerNode   the corresponding transducerNode
 * @param databaseNode     the corresponding databaseNode
 * @param initialState boolean for being an initial state
 * @param finalState   boolean for being a final state
 */

class ProductAutomatonNode(
    val queryNode: QueryNode,
    val transducerNode: TransducerNode,
    val databaseNode: DatabaseNode,
    val initialState: Boolean,
    val finalState: Boolean,
): Comparable<ProductAutomatonNode> {

    var identifier: Triple<QueryNode, TransducerNode, DatabaseNode> = Triple(queryNode, transducerNode, databaseNode)
    var isInitialState: Boolean = initialState
    var isFinalState: Boolean = finalState
    var weight: Double = Double.POSITIVE_INFINITY
    var edges: HashSet<ProductAutomatonEdge> = hashSetOf()

    override fun compareTo(other: ProductAutomatonNode): Int {
        if (this.weight == other.weight) {
            // If weights are equal, compare based on identifier or any other desired criteria
            // For example:
            // return this.identifier.compareTo(other.identifier)
            return 0
        }
        if (this.weight == Double.POSITIVE_INFINITY) {
            return 1 // Only this node has positive infinity weight, consider other node greater
        }
        if (other.weight == Double.POSITIVE_INFINITY) {
            return -1 // Only the other node has positive infinity weight, consider this node greater
        }
        return this.weight.compareTo(other.weight)
    }

    fun equalsWithoutEdges(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ProductAutomatonNode) return false

        return queryNode == other.queryNode &&
                transducerNode == other.transducerNode &&
                databaseNode == other.databaseNode &&
                initialState == other.initialState &&
                finalState == other.finalState;
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ProductAutomatonNode) return false

        return queryNode == other.queryNode &&
                transducerNode == other.transducerNode &&
                databaseNode == other.databaseNode &&
                initialState == other.initialState &&
                finalState == other.finalState &&
                edges == other.edges;
    }

    override fun hashCode(): Int {
        var result = queryNode.hashCode()
        result = 31 * result + transducerNode.hashCode()
        result = 31 * result + databaseNode.hashCode()
        result = 31 * result + initialState.hashCode()
        result = 31 * result + finalState.hashCode()
        return result
    }


    /**
     * simple print method.
     */
    fun print() {
        println(toString())
    }

    fun printWithWeight() {
        println(toStringWithWeight())
    }

    override fun toString(): String {
        return String.format(
            "(%s, %s, %s)",
            identifier.first.identifier,
            identifier.second.identifier,
            identifier.third.identifier
        )
    }

    private fun toStringWithWeight(): String {
        return if (weight == POSITIVE_INFINITY) {
            String.format(
                "(%s, %s, %s)[INF]",
                identifier.first.identifier,
                identifier.second.identifier,
                identifier.third.identifier
            )
        } else String.format(
            "(%s, %s, %s)[%s]",
            identifier.first.identifier,
            identifier.second.identifier,
            identifier.third.identifier,
            weight
        )
    }

    val identifierString: String
        get() = String.format(
            "%s|%s|%s",
            identifier.first.identifier,
            identifier.second.identifier,
            identifier.third.identifier
        )

}
