package tin.model.graph

import tin.model.alphabet.Alphabet
import kotlin.collections.HashSet

abstract class Graph {
    abstract val nodes: NodeSet<out Node>;
    var alphabet: Alphabet = Alphabet();

    /** helper function to use in the context of populating the graph only.
     * Note that since we cannot have different nodes with the same identifier,
     * we just have to check the identifier property and no other properties.
     */
    protected fun findNodeWithoutEdgeComparison(node: Node): Node? {
        return getNode(node.identifier);
    }

    open fun printGraph() {
        for (node in nodes) {
            for (edge in node.edges) {
                edge.print()
            }
        }
    }

    open fun getNode(identifier: String) : Node? {
        return nodes.find {
            it.identifier == identifier
        }
    }


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


}