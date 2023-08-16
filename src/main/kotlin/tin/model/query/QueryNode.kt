package tin.model.query

import kotlin.collections.HashSet

class QueryNode(
    val identifier: String,
    val isInitialState: Boolean,
    val isFinalState: Boolean,
    val edges: HashSet<QueryEdge>
) {

    constructor(
        identifier: String,
        isInitialState: Boolean,
        isFinalState: Boolean
    ) : this(
        identifier = identifier,
        isInitialState = isInitialState,
        isFinalState = isFinalState,
        edges = hashSetOf()
    )

    /**
     * plain QueryNode.equals() and QueryEdge.equals() methods will cause a circular dependency and stack overflows.
     * It is more important to check here if edges set is equal since we have to trim the equals() method in the Edge class.
     * That will resolve the circular dependency.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is QueryNode) return false

        return this.equalsWithoutEdges(other) &&
                edges == other.edges
    }

    /**
     * we need all properties to be checked because we use this as an equals() method
     * We must not check for edges == other.edges but we can check their size to prevent at least some false positives.
     */
    fun equalsWithoutEdges(other: Any?): Boolean {
        if (this === other) return true
        if (other !is QueryNode) return false

        return identifier == other.identifier &&
                isInitialState == other.isInitialState &&
                isFinalState == other.isFinalState &&
                edges.size == other.edges.size
    }

    override fun hashCode(): Int {
        var result = identifier.hashCode()
        result = 31 * result + isInitialState.hashCode()
        result = 31 * result + isFinalState.hashCode()
        result = 31 * result + edges.hashCode()
        return result
    }
}
