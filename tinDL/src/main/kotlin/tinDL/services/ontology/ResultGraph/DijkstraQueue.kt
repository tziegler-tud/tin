package tinDL.services.ontology.ResultGraph

import tinDL.model.v2.ResultGraph.ResultNode
import tinDL.model.v2.ResultGraph.ResultNodeSet

class DijkstraQueue: HashSet<ResultNode>() {
    fun addNodeset(nodeset: ResultNodeSet) {
        nodeset.forEach { node ->
            this.add(node.asResultNode()!!)
        }
    }

    fun addNode(node: ResultNode) {
        this.add(node)
    }

    fun removeNode(node: ResultNode) {
        this.remove(node)
    }
}