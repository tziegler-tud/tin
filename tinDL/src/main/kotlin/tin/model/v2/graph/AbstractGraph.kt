package tin.model.v2.graph

import tin.model.v1.alphabet.Alphabet

abstract class AbstractGraph : Graph {

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

    abstract override val nodes: NodeSet;
    abstract override val edges: EdgeSet<out Edge>
    abstract override var alphabet: Alphabet;

    override fun addNode(node: Node) : Boolean {
        return nodes.add(node);
    }

    override fun addNodes(vararg n: Node) : Boolean {
        val list = listOf(*n);
        var allInserted = true
        list.forEach { allInserted = allInserted && addNode(it) }
        return allInserted;
    }

    override fun getNode(identifier: String) : Node? {
        return nodes.get(identifier)
    }

    override fun containsNode(identifier: String) : Boolean {
        return nodes.find { it.identifier == identifier } != null
    }

    override fun containsNode(node: Node) : Boolean {
        return nodes.contains(node)
    }

    override fun containsEdge(edge: Edge) : Boolean {
        return edges.contains(edge)
    }

    override fun getEdgesWithSource(source: Node): List<Edge> {
        return edges.filterForSource(source);
    }

    override fun getEdgesWithTarget(target: Node): List<Edge> {
        return edges.filterForTarget(target);
    }

    override fun getEdgesWithSourceAndTarget(source: Node, target: Node): List<Edge> {
        return edges.filterForSourceAndTarget(source, target);
    }

    override fun getEdgesWithLabel(label: EdgeLabel): List<Edge> {
        return edges.filterForLabel(label);
    }

    override fun getInitialNodes() : List<Node> {
        return this.nodes.filter { it.isInitialState };
    }

    override fun getFinalNodes(): List<Node> {
        return this.nodes.filter { it.isFinalState };
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Graph) return false

        return hasEqualNodes(other) && hasEqualEdges(other) && (alphabet == other.alphabet)

    }

    fun hasEqualNodes(other: Graph) : Boolean {
        if (nodes.count() != other.nodes.count()) return false
        nodes.forEach {
            val node = other.getNode(it.identifier);
            if (node !== null) {
                if (it != node) return false;
            } else return false;
        }
        return true;
    }

    fun hasEqualEdges(other: Graph) : Boolean {
        if (edges.count() != other.edges.count()) return false
        edges.forEach {
            if (!other.containsEdge(it)) {
                return false;
            }
        }
        return true;
    }

    override fun hashCode(): Int {
        var result = nodes.hashCode()
        result = 31 * result + edges.hashCode();
        result = 31 * result + alphabet.hashCode()
        return result
    }

    override fun isEmpty(): Boolean {
        return nodes.isEmpty()
    }

    override fun isValidGraph(): Boolean {
        return hasInitialNode() && hasFinalNode()
    }

    override fun hasInitialNode(): Boolean{
        return nodes.any{it.isInitialState}
    }
    override fun hasFinalNode(): Boolean{
        return nodes.any{it.isFinalState}
    }

    override fun printGraph() {
        for (edge in edges) {
            edge.print();
        }
    }
    abstract override fun addEdge(edge: Edge) : Boolean;
}