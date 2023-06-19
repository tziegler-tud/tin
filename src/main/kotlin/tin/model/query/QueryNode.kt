package tin.model.query

import tin.utils.compareEdgeSets
import java.util.*

class QueryNode(
        val identifier: String,
        val isInitialState: Boolean,
        val isFinalState: Boolean,
        val edges: LinkedList<QueryEdge>) {

    constructor(identifier: String,
                isInitialState: Boolean,
                isFinalState: Boolean
    ) : this(
            identifier = identifier,
            isInitialState = isInitialState,
            isFinalState = isFinalState,
            edges = LinkedList())

    fun equals(otherNode: QueryNode): Boolean {
        // compare basic parameters
        if (equalsExcludingEdges(otherNode)) {
            // compare edges
            if ((this.edges.size == 0).xor(otherNode.edges.size == 0)) {
                // if one has no edges but the other has -> not equals
                return false
            } else if (this.edges.size == 0 && otherNode.edges.size == 0) {
                // if both have no edges -> equals
                return true
            }


            return if (this.edges.size != otherNode.edges.size) {
                false
            } else compareEdgeSets(this.edges, otherNode.edges)

        }
        return false
    }


    fun equalsExcludingEdges(otherNode: QueryNode): Boolean {
        return isInitialState == otherNode.isInitialState && isFinalState == otherNode.isFinalState && identifier == otherNode.identifier
    }
}
