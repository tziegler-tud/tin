package tin.model.transducer

import java.util.*

class TransducerGraph {
    var nodes: MutableSet<TransducerNode> = HashSet()

    fun addTransducerObjectNode(vararg n: TransducerNode) {
        nodes.addAll(listOf(*n))
    }

    fun addTransducerObjectEdge(source: TransducerNode, target: TransducerNode, incoming: String, outgoing: String, cost: Double) {
        nodes.add(source)
        nodes.add(target)

        // don't add duplicate edges!
        for (edge in source.edges!!) {
            if ((edge.source.equals(source)) && (edge.target.equals(target)) &&
                    edge.incomingString.equals(incoming, ignoreCase = true) &&
                    edge.outgoingString.equals(outgoing, ignoreCase = true) && (edge.cost == cost)) {
                return
            }
        }
        source.edges.add(TransducerEdge(source, target, incoming, outgoing, cost))
    }

    fun getNodes(): Set<TransducerNode> {
        return nodes
    }

    // TODO: print isolated nodes. (same as in the query)
    fun printGraph() {
        for (node in nodes) {
            for (edge in node.edges!!) {
                edge.print()
            }
        }
    }

    // only use this for test purposes!
    fun equals(otherGraph: TransducerGraph): Boolean {
        return if (nodes.size != otherGraph.nodes.size) {
            false
        } else equalsOtherNodeSet(otherGraph.nodes)
    }

    private fun equalsOtherNodeSet(otherNodeSet: Set<TransducerNode>?): Boolean {
        var pairsFound = 0
        for (thisNode in nodes) {
            for (otherNode in otherNodeSet!!) {
                if (thisNode.equals(otherNode)) {
                    pairsFound++
                    break
                }
            }
        }
        return pairsFound == nodes.size
    }
}
