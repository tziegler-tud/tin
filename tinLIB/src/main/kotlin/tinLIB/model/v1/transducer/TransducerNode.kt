package tinLIB.model.v1.transducer

import tinLIB.model.v1.graph.EdgeSet
import tinLIB.model.v1.graph.Node

class TransducerNode(
        identifier: String,
        isInitialState: Boolean,
        isFinalState: Boolean,
        edges: EdgeSet<TransducerEdge> = EdgeSet()
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

    override var edges: EdgeSet<TransducerEdge> = EdgeSet();

    fun addEdge(edge: TransducerEdge) {
        edges.add(edge);
    }

    /**
     * plain TransducerNode.equals() and TransducerEdge.equals() methods will cause a circular dependency and stack overflows.
     * It is more important to check here if edges set is equal since we have to trim the equals() method in the Edge class.
     * That will resolve the circular dependency.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TransducerNode) return false

        return super.equals(other);
//        return this.equalsWithoutEdges(other) &&
//                edges == other.edges
    }

    override fun hashCode(): Int {
        return super.hashCode();
    }
}
