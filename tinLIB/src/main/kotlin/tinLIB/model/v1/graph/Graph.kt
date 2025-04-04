package tinLIB.model.v1.graph

import tinLIB.model.v1.alphabet.Alphabet

abstract class Graph {

    companion object {
        fun isValidGraph(graphLikeObject: Any?): Boolean {
            //must be a Graph
            if (graphLikeObject !is Graph){
                return false;
            }
            //at least one initial node and one final node
            return graphLikeObject.hasInitialNode() && graphLikeObject.hasFinalNode();
        }
    }

    abstract val nodes: NodeSet<out Node>;
    var alphabet: Alphabet = Alphabet();

    /** helper function to use in the context of populating the graph only.
     * Note that since we cannot have different nodes with the same identifier,
     * we just have to check the identifier property and no other properties.
     */
    protected fun findNodeWithoutEdgeComparison(node: Node): Node? {
        return getNode(node.identifier);
    }

    abstract fun printGraph()

    abstract fun getNode(identifier: String) : Node?


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Graph) return false

        nodes.forEach {
            val node = other.getNode(it.identifier);
            if (node !== null) {
                if (it != node) return false;
            } else return false;
        }
        return alphabet == other.alphabet;

    }

    override fun hashCode(): Int {
        var result = nodes.hashCode()
        result = 31 * result + alphabet.hashCode()
        return result
    }

    final fun isEmpty(): Boolean {
        return nodes.isEmpty()
    }

    final fun isValidGraph(): Boolean {
        return hasInitialNode() && hasFinalNode()
    }

    final fun hasInitialNode(): Boolean{
        return nodes.any{it.isInitialState}
    }
    final fun hasFinalNode(): Boolean{
        return nodes.any{it.isFinalState}
    }


}