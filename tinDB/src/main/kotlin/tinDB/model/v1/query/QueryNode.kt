package tinDB.model.v1.query

import tinDB.model.v1.graph.EdgeSet
import tinDB.model.v1.graph.Node

class QueryNode(
    identifier: String,
    isInitialState: Boolean,
    isFinalState: Boolean,
    edges: EdgeSet<QueryEdge>
) : Node(
        identifier, isInitialState, isFinalState, edges
){

    constructor(
        identifier: String,
        isInitialState: Boolean,
        isFinalState: Boolean
    ) : this(
        identifier = identifier,
        isInitialState = isInitialState,
        isFinalState = isFinalState,
        edges = EdgeSet()
    )

    override var edges: EdgeSet<QueryEdge> = EdgeSet();

    fun addEdge(edge: QueryEdge) {
        edges.add(edge);
    }

    /**
     * plain QueryNode.equals() and QueryEdge.equals() methods will cause a circular dependency and stack overflows.
     * It is more important to check here if edges set is equal since we have to trim the equals() method in the Edge class.
     * That will resolve the circular dependency.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is QueryNode) return false

        return super.equals(other);
//        return this.equalsWithoutEdges(other) &&
//                edges == other.edges
    }

    /**
     * we need all properties to be checked because we use this as an equals() method
     * We must not check for edges == other.edges but we can check their size to prevent at least some false positives.
     */
    override fun equalsWithoutEdges(other: Any?): Boolean {
        if (this === other) return true
        if (other !is QueryNode) return false

        return super.equalsWithoutEdges(other);

//        return identifier == other.identifier &&
//                isInitialState == other.isInitialState &&
//                isFinalState == other.isFinalState &&
//                edges.size == other.edges.size
    }

    override fun hashCode(): Int {
        return super.hashCode();
    }
}
