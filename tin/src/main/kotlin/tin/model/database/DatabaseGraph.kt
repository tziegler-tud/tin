package tin.model.database

import tin.model.compareNodeSets
import java.util.*

class DatabaseGraph {

    var nodes: MutableSet<DatabaseNode> = HashSet()
    var alphabet: Set<String> = HashSet()

    fun addNodes(vararg n: DatabaseNode) {
        nodes.addAll(listOf(*n))
    }

    fun addEdge(source: DatabaseNode, target: DatabaseNode, label: String) {
        nodes.add(source)
        nodes.add(target)

        // don't add duplicate edges!
        for (edge in source.edges) {
            if (edge.source == source && edge.target == target && edge.label.equals(label, ignoreCase = true)) {
                return
            }
        }
        source.edges.add(DatabaseEdge(source, target, label))
    }



    fun printGraph() {
        for (node in nodes) {
            for (edge in node.edges) {
                edge.print()
            }
        }
    }

    // only use this for test purposes!
    fun equals(otherGraph: DatabaseGraph): Boolean {
        if (nodes.size == 0 || otherGraph.nodes.size == 0) {
            return false
        }
        return if (nodes.size != otherGraph.nodes.size) {
            false
        } else compareNodeSets(this.nodes, otherGraph.nodes)
    }


}