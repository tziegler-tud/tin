package tin.model.transducer

import kotlin.collections.HashSet

class TransducerNode(
    val identifier: String,
    val initialState: Boolean,
    val finalState: Boolean,
    val edges: HashSet<TransducerEdge>? = hashSetOf()
) {
    /**
     * plain TransducerNode.equals() and TransducerEdge.equals() methods will cause a circular dependency and stack overflows.
     * It is more important to check here if edges set is equal since we have to trim the equals() method in the Edge class.
     * That will resolve the circular dependency.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TransducerNode) return false

        return this.equalsWithoutEdges(other) &&
                edges == other.edges
    }

    /**
     * we need all properties to be checked because we use this as an equals() method
     * We must not check for edges == other.edges but we can check their size to prevent at least some false positives.
     */
    fun equalsWithoutEdges(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TransducerNode) return false

        return identifier == other.identifier &&
                initialState == other.initialState &&
                finalState == other.finalState &&
                edges?.size == other.edges?.size
    }

    override fun hashCode(): Int {
        var result = identifier.hashCode()
        result = 31 * result + initialState.hashCode()
        result = 31 * result + finalState.hashCode()
        result = 31 * result + (edges?.hashCode() ?: 0)
        return result
    }
}
