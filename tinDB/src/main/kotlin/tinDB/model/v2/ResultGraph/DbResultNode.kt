package tinDB.model.v2.ResultGraph

import tinDB.model.v2.productAutomaton.ProductAutomatonNode
import tinLIB.model.v2.ResultGraph.ResultNode
import tinLIB.model.v2.graph.Node

class DbResultNode(
    private val queryNode: Node,
    private val transducerNode: Node,
    override val individual: DbResultGraphIndividual
) : ResultNode(queryNode, transducerNode, individual)

{
    constructor(queryNode: Node, transducerNode: Node, databaseNode: Node ): this(queryNode, transducerNode, DbResultGraphIndividual(databaseNode))

    constructor(productAutomatonNode: ProductAutomatonNode): this(
        productAutomatonNode.queryNode,
        productAutomatonNode.transducerNode,
        productAutomatonNode.databaseNode
    )
    override var isInitialState: Boolean = queryNode.isInitialState && transducerNode.isInitialState
    override var isFinalState: Boolean = queryNode.isFinalState && transducerNode.isFinalState;

    /**
     * plain QueryNode.equals() and QueryEdge.equals() methods will cause a circular dependency and stack overflows.
     * It is more important to check here if edges set is equal since we have to trim the equals() method in the Edge class.
     * That will resolve the circular dependency.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DbResultNode) return false

        return queryNode == other.queryNode &&
                transducerNode == other.transducerNode &&
                individual == other.individual &&
                isInitialState == other.isInitialState &&
                isFinalState == other.isFinalState
    }

    override fun hashCode(): Int {
        var result = identifier.hashCode()
        result = 31 * result + isInitialState.hashCode()
        result = 31 * result + isFinalState.hashCode()
        return result
    }
}