package tinLIB.model.v2.ResultGraph

import tinLIB.model.v2.graph.Node

open class ResultNode(
    private val queryNode: Node,
    private val transducerNode: Node,
    open val individual: ResultGraphIndividual
) : Node(queryNode.identifier + transducerNode.identifier + individual.identifier, queryNode.isInitialState && transducerNode.isInitialState, queryNode.isFinalState && transducerNode.isFinalState)

{
    override var isInitialState: Boolean = queryNode.isInitialState && transducerNode.isInitialState
    override var isFinalState: Boolean = queryNode.isFinalState && transducerNode.isFinalState;

    /**
     * plain QueryNode.equals() and QueryEdge.equals() methods will cause a circular dependency and stack overflows.
     * It is more important to check here if edges set is equal since we have to trim the equals() method in the Edge class.
     * That will resolve the circular dependency.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ResultNode) return false

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