package tin.model.productAutomaton

import tin.model.database.DatabaseNode
import tin.model.query.QueryNode
import tin.model.transducer.TransducerNode
import java.lang.Double.POSITIVE_INFINITY
import java.util.*

/**
 * constructor for a productAutomatonNode. it has the form
 * (queryNode, transducerNode, databaseNode) and has booleans for being an initial or final state
 *
 * @param qNode        the corresponding queryNode
 * @param tNode        the corresponding transducerNode
 * @param dNode        the corresponding databaseNode
 * @param initialState boolean for being an initial state
 * @param finalState   boolean for being a final state
 */

class ProductAutomatonNode(
        queryNode: QueryNode,
        transducerNode: TransducerNode,
        databaseNode: DatabaseNode,
        initialState: Boolean,
        finalState: Boolean,
        ) {

    var identifier: Triple<QueryNode, TransducerNode, DatabaseNode>
    var isInitialState: Boolean = initialState
    var isFinalState: Boolean = finalState
    var weight: Double
    var edges: LinkedList<ProductAutomatonEdge>


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
        return String.format("(%s, %s, %s)", identifier.first.identifier, identifier.second.identifier, identifier.third.identifier)
    }

    fun toStringWithWeight(): String {
        return if (weight == POSITIVE_INFINITY) {
            String.format("(%s, %s, %s)[INF]", identifier.first.identifier, identifier.second.identifier, identifier.third.identifier)
        } else String.format("(%s, %s, %s)[%s]", identifier.first.identifier, identifier.second.identifier, identifier.third.identifier, weight)
    }

    val identifierString: String
        get() = String.format("%s|%s|%s", identifier.first.identifier, identifier.second.identifier, identifier.third.identifier)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ProductAutomatonNode) return false

        return equalsExcludingEdges(other) && edges.size == other.edges.size && equalsOtherEdgeSet(other.edges)
    }

    private fun equalsOtherEdgeSet(otherEdgeList: LinkedList<ProductAutomatonEdge>?): Boolean {
        var pairsFound = 0
        for (thisEdge in edges) {
            for (otherEdge in otherEdgeList!!) {
                if (thisEdge.equals(otherEdge)) {
                    pairsFound++
                    break
                }
            }
        }
        return pairsFound == edges.size
    }

    fun equalsExcludingEdges(otherNode: ProductAutomatonNode): Boolean {
        return isInitialState == otherNode.isInitialState && isFinalState == otherNode.isFinalState && weight == otherNode.weight && checkIdentifierEquality(otherNode)
    }

    private fun checkIdentifierEquality(otherNode: ProductAutomatonNode): Boolean {
        return identifier.first.equals(otherNode.identifier.first) &&
                identifier.second.equals(otherNode.identifier.second) &&
                identifier.third.equals(otherNode.identifier.third)
    }

    override fun hashCode(): Int {
        var result = identifier.hashCode()
        result = 31 * result + isInitialState.hashCode()
        result = 31 * result + isFinalState.hashCode()
        result = 31 * result + weight.hashCode()
        result = 31 * result + edges.hashCode()
        return result
    }

    init {
        this.identifier = Triple(queryNode, transducerNode, databaseNode)
        this.weight = Double.NaN
        this.edges = LinkedList()
    }
}
