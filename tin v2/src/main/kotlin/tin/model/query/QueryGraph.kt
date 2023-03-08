package tin.model.query

import java.util.HashSet

class QueryGraph {
    var nodes: MutableSet<QueryNode> = HashSet()

    fun addQueryNodes(vararg n: QueryNode) {
        nodes.addAll(listOf(*n))
    }

    fun addQueryEdge(source: QueryNode, target: QueryNode, label: String) {

        // .add on a hashSet does not add an element that is already present! We don't need to check whether the node is already created.
        nodes.add(source)
        nodes.add(target)

        // if this edge is already present -> don't add it.
        for (edge in source.edges) {
            if ((edge.source == source) && (edge.target == target) && edge.label.equals(label, ignoreCase = true)) {
                return
            }
        }
        source.edges.add(QueryEdge(source, target, label))
    }

    fun printGraph() {
        for (node in nodes) {
            for (edge in node.edges) {
                edge.print()
            }
        }
    }


    // only use this for test purposes!
    fun equals(otherGraph: QueryGraph): Boolean {
        return if (nodes.size != otherGraph.nodes.size) {
            false
        } else equalsOtherNodeSet(otherGraph.nodes)
    }

    private fun equalsOtherNodeSet(otherNodeSet: Set<QueryNode>): Boolean {
        var pairsFound = 0
        for (thisNode in nodes) {
            for (otherNode in otherNodeSet) {
                if (thisNode.equals(otherNode)) {
                    pairsFound++
                    break
                }
            }
        }
        return pairsFound == nodes.size
    }


}