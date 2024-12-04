package tin.model.v2.graph

import tin.model.v2.ResultGraph.ResultNode
import tin.model.v2.genericGraph.PairNode

open class Node(
        var identifier: String,
        var isInitialState : Boolean = false,
        var isFinalState : Boolean = false,
    )
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

    override fun toString(): String {
        return this.identifier;
    }

    open fun asPairNode(): PairNode? {
        return null;
    }

    open fun asResultNode(): ResultNode? {
        return null;
    }
}
