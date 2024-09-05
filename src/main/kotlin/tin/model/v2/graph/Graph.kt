package tin.model.v2.graph

import tin.model.v1.alphabet.Alphabet
import tin.model.v2.query.QueryEdge

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

    abstract val nodes: NodeSet;
    abstract val edges: EdgeSet<out Edge>
    var alphabet: Alphabet = Alphabet();

    open fun addNode(node: Node) : Boolean {
        return nodes.add(node);
    }

    open fun addNodes(vararg n: Node){
        val list = listOf(*n);
        list.forEach { addNode(it) }
    }

    fun getNode(identifier: String) : Node? {
        return nodes.get(identifier)
    }

    fun containsNode(identifier: String) : Boolean {
        return nodes.find { it.identifier == identifier } != null
    }

    fun containsNode(node: Node) : Boolean {
        return nodes.contains(node)
    }

    open fun containsEdge(edge: Edge) : Boolean {
        return edges.contains(edge)
    }

    open fun getEdgesWithSource(source: Node): List<Edge> {
        return edges.filterForSource(source);
    }

    open fun getEdgesWithTarget(target: Node): List<Edge> {
        return edges.filterForTarget(target);
    }

    open fun getEdgesWithSourceAndTarget(source: Node, target: Node): List<Edge> {
        return edges.filterForSourceAndTarget(source, target);
    }

    open fun getEdgesWithLabel(label: String): List<Edge> {
        return edges.filterForLabel(label);
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
        edges.forEach {
            if (!other.edges.contains(it)) {
                return false;
            }
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

    open fun printGraph() {
        for (edge in edges) {
            edge.print();
        }
    }
    abstract fun addEdge(edge: Edge) : Boolean;
}