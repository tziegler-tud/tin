package tinDB.model.v2.productAutomaton

import tinLIB.model.v2.graph.Node

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
    val queryNode: Node,
    val transducerNode: Node,
    val databaseNode: Node,
    initialState: Boolean? = null,
    finalState: Boolean? = null,
): Node(
    identifier="(${queryNode.identifier}, ${transducerNode.identifier}, ${databaseNode.identifier})",
    isInitialState = initialState?: queryNode.isInitialState && transducerNode.isInitialState,
    isFinalState = finalState?: queryNode.isFinalState && transducerNode.isFinalState,
) {

    var weight: Double = Double.POSITIVE_INFINITY
    private var internalIdentifier: Triple<Node, Node, Node> = Triple(queryNode, transducerNode, databaseNode)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ProductAutomatonNode) return false

        return queryNode == other.queryNode &&
                transducerNode == other.transducerNode &&
                databaseNode == other.databaseNode &&
                isInitialState == other.isInitialState &&
                isFinalState == other.isFinalState
    }

    override fun equalsWithoutState(other: Any): Boolean {
        if(this === other) return true
        if(other !is ProductAutomatonNode) return false

        return queryNode == other.queryNode &&
                transducerNode == other.transducerNode &&
                databaseNode == other.databaseNode
    }

    override fun hashCode(): Int {
        var result = queryNode.hashCode()
        result = 31 * result + transducerNode.hashCode()
        result = 31 * result + databaseNode.hashCode()
        result = 31 * result + isInitialState.hashCode()
        result = 31 * result + isFinalState.hashCode()
        return result
    }


    /**
     * simple print method.
     */
    fun print() {
        println(toString())
    }


    val identifierString: String
        get() = String.format(
            "%s|%s|%s",
            internalIdentifier.first.identifier,
            internalIdentifier.second.identifier,
            internalIdentifier.third.identifier
        )

}
