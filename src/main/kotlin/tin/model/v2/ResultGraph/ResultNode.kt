package tin.model.v2.ResultGraph

import org.semanticweb.owlapi.model.OWLNamedIndividual
import tin.model.v2.graph.Node

class ResultNode(
    private val queryNode: Node,
    private val transducerNode: Node,
    private val individual: OWLNamedIndividual
) : Node(queryNode.identifier+transducerNode.identifier+individual.toString(), false, false)

{
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
                isFinalState == other.isFinalState;
    }

    fun getQueryNode(): Node {
        return queryNode;
    }

    fun getTransducerNode(): Node {
        return transducerNode;
    }

    override fun hashCode(): Int {
        var result = identifier.hashCode()
        result = 31 * result + isInitialState.hashCode()
        result = 31 * result + isFinalState.hashCode()
        return result
    }
}