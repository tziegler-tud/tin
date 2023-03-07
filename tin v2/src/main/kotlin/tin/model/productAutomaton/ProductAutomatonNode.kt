package tin.model.productAutomaton

import org.javatuples.Triplet
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

    var identifier: Triplet<QueryNode, TransducerNode, DatabaseNode>
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
        return String.format("(%s, %s, %s)", identifier!!.value0.identifier, identifier!!.value1.identifier, identifier!!.value2.identifier)
    }

    fun toStringWithWeight(): String {
        return if (weight == POSITIVE_INFINITY) {
            String.format("(%s, %s, %s)[INF]", identifier!!.value0.identifier, identifier!!.value1.identifier, identifier!!.value2.identifier)
        } else String.format("(%s, %s, %s)[%s]", identifier!!.value0.identifier, identifier!!.value1.identifier, identifier!!.value2.identifier, weight)
    }

    val identifierString: String
        get() = String.format("%s|%s|%s", identifier!!.value0.identifier, identifier!!.value1.identifier, identifier!!.value2.identifier)

    fun equals(otherNode: ProductAutomatonNode): Boolean {
        // compare basic parameters
        if (equalsExcludingEdges(otherNode)) {
            // compare edges
            run {
                if (this.edges == null || otherNode.edges == null) {
                    return false
                }
                return if (this.edges!!.size != otherNode.edges!!.size) {
                    false
                } else equalsOtherEdgeSet(otherNode.edges)
            }
        }
        return false
    }

    private fun equalsOtherEdgeSet(otherEdgeList: LinkedList<ProductAutomatonEdge>?): Boolean {
        var pairsFound = 0
        for (thisEdge in edges!!) {
            for (otherEdge in otherEdgeList!!) {
                if (thisEdge.equals(otherEdge)) {
                    pairsFound++
                    break
                }
            }
        }
        return pairsFound == edges!!.size
    }

    fun equalsExcludingEdges(otherNode: ProductAutomatonNode): Boolean {
        return isInitialState == otherNode.isInitialState && isFinalState == otherNode.isFinalState && weight == otherNode.weight && checkIdentifierEquality(otherNode)
    }

    private fun checkIdentifierEquality(otherNode: ProductAutomatonNode): Boolean {
        return identifier!!.value0.equals(otherNode.identifier!!.value0) &&
                identifier!!.value1.equals(otherNode.identifier!!.value1) &&
                identifier!!.value2.equals(otherNode.identifier!!.value2)
    }

    init {
        this.identifier = Triplet.with(queryNode, transducerNode, databaseNode)
        this.weight = Double.NaN
        this.edges = LinkedList()
    }
}
