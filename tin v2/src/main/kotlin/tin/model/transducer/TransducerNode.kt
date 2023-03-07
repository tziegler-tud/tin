package tin.model.transducer

import tin.model.compareEdgeSets
import java.util.*

class TransducerNode(
        val identifier: String,
        val isInitialState: Boolean,
        val isFinalState: Boolean,
        val edges: LinkedList<TransducerEdge>?
) {

    fun equals(otherNode: TransducerNode): Boolean {
        // compare basic parameters
        if (compareToExcludingEdges(otherNode)) {
            // compare edges
            run {
                if (this.edges == null || otherNode.edges == null) {
                    return false
                }
                return if (this.edges.size != otherNode.edges.size) {
                    false
                } else compareEdgeSets(this.edges, otherNode.edges)
            }
        }
        return false
    }


    fun compareToExcludingEdges(otherNode: TransducerNode): Boolean {
        return isInitialState == otherNode.isInitialState && isFinalState == otherNode.isFinalState && identifier == otherNode.identifier
    }
}
