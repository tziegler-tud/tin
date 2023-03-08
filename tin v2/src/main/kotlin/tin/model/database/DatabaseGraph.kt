package tin.model.database

import tin.model.compareNodeSets
import java.util.*

class DatabaseGraph {

    var nodes: MutableSet<DatabaseNode>? = HashSet()


    fun addDatabaseObjectNode(vararg n: DatabaseNode) {
        nodes!!.addAll(listOf(*n))
    }

    fun addDatabaseObjectEdge(source: DatabaseNode, target: DatabaseNode, label: String) {
        nodes!!.add(source)
        nodes!!.add(target)

        // don't add duplicate edges!
        for (edge in source.edges) {
            if (edge.source == source && edge.target == target && edge.label.equals(label, ignoreCase = true)) {
                return
            }
        }
        source.edges.add(DatabaseEdge(source, target, label))
    }



    fun printGraph() {
        for (node in nodes!!) {
            for (edge in node.edges) {
                edge.print()
            }
        }
    }

    // only use this for test purposes!
    fun equals(otherGraph: DatabaseGraph): Boolean {
        if (nodes == null || otherGraph.nodes == null) {
            return false
        }
        return if (nodes!!.size != otherGraph.nodes!!.size) {
            false
        } else compareNodeSets(this.nodes!!, otherGraph.nodes!!)
    }


}