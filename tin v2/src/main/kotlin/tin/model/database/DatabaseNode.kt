package tin.model.database

import tin.model.compareEdgeSets
import java.util.*


class DatabaseNode(
        var identifier: String,
        var edges: LinkedList<DatabaseEdge> = LinkedList()
) {

    fun getIdentifier(): String {
        return identifier
    }

    fun getEdges(): LinkedList<DatabaseEdge> {
        return edges
    }

    fun equals(otherNode: DatabaseNode): Boolean {
        // compare basic parameters
        if (compareToExcludingEdges(otherNode)) {
            // compare edges
            run {
                return if (this.edges.size != otherNode.edges.size) {
                    false
                } else compareEdgeSets(this.edges, otherNode.edges)
            }
        }
        return false
    }


    fun compareToExcludingEdges(otherNode: DatabaseNode): Boolean {
        return identifier == otherNode.identifier
    }

}