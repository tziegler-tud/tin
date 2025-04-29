package tinLIB.services.ResultGraph

import tinLIB.model.v2.ResultGraph.ResultNode
import tinLIB.model.v2.ResultGraph.ResultNodeSet

open class DijkstraQueue<T: ResultNode>: HashSet<T>() {
    fun addNodeset(nodeset: ResultNodeSet<T>) {
        nodeset.forEach { node ->
            this.add(node)
        }
    }

    fun addNode(node: T) {
        this.add(node)
    }

    fun removeNode(node: T) {
        this.remove(node)
    }
}