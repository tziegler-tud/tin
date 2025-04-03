package tinDL.model.v2.genericGraph

import tinDL.model.v2.graph.Node

class S4Node(
    private val queryNode: Node,
    private val transducerNode: Node,
) : Node(queryNode.toString()+transducerNode.toString(), false, false)

{
    /**
     * plain QueryNode.equals() and QueryEdge.equals() methods will cause a circular dependency and stack overflows.
     * It is more important to check here if edges set is equal since we have to trim the equals() method in the Edge class.
     * That will resolve the circular dependency.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Node) return false

        return identifier == other.identifier &&
                isInitialState == other.isInitialState &&
                isFinalState == other.isFinalState;
    }

    override fun hashCode(): Int {
        var result = identifier.hashCode()
        result = 31 * result + isInitialState.hashCode()
        result = 31 * result + isFinalState.hashCode()
        return result
    }
}