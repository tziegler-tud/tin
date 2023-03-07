package tin.model.query

import java.util.*

class QueryNode(
        val identifier: String,
        val isInitialState: Boolean,
        val isFinalState: Boolean,
        val edges: LinkedList<QueryEdge>
) {
    fun equals(otherNode: QueryNode): Boolean {
        // compare basic parameters
        if (equalsExcludingEdges(otherNode)) {
            // compare edges
            run {
                return if (this.edges.size != otherNode.edges.size) {
                    false
                } else equalsOtherEdgeSet(otherNode.edges)
            }
        }
        return false
    }

    private fun equalsOtherEdgeSet(otherEdgeList: LinkedList<QueryEdge>?): Boolean {
        var pairsFound = 0
        for (thisEdge in edges!!) {
            for (otherEdge in otherEdgeList!!) {
                if (thisEdge.equals(otherEdge)) {
                    pairsFound++
                    break
                }
            }
        }
        return pairsFound == edges.size
    }

    fun equalsExcludingEdges(otherNode: QueryNode): Boolean {
        return isInitialState == otherNode.isInitialState && isFinalState == otherNode.isFinalState && identifier == otherNode.identifier
    }
}
