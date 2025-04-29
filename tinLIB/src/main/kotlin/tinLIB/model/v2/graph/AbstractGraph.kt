package tinLIB.model.v2.graph

import tinLIB.model.v2.alphabet.Alphabet

abstract class AbstractGraph<T: Node, E: Edge> : Graph<T,E> {

    companion object {
        fun isValidGraph(graphLikeObject: Any?): Boolean {
            //must be a Graph
            if (graphLikeObject !is Graph<*,*>){
                return false;
            }
            //at least one initial node and one final node
            return graphLikeObject.hasInitialNode() && graphLikeObject.hasFinalNode();
        }
    }

    abstract override val nodes: NodeSet<T>;
    abstract override val edges: EdgeSet<out E>
    abstract override var alphabet: Alphabet;

    override fun addNode(node: T) : Boolean {
        return nodes.add(node);
    }

    override fun addNodes(vararg n: T) : Boolean {
        val list = listOf(*n);
        var allInserted = true
        list.forEach { allInserted = allInserted && addNode(it) }
        return allInserted;
    }

    override fun getNode(identifier: String) : T? {
        return nodes.get(identifier)
    }

    override fun containsNode(identifier: String) : Boolean {
        return nodes.find { it.identifier == identifier } != null
    }

    override fun containsNode(node: T) : Boolean {
        return nodes.contains(node)
    }

    override fun containsEdge(edge: E) : Boolean {
        return edges.contains(edge)
    }

    override fun getEdgesWithSource(source: T): List<E> {
        return edges.filterForSource(source);
    }

    override fun getEdgesWithTarget(target: T): List<E> {
        return edges.filterForTarget(target);
    }

    override fun getEdgesWithSourceAndTarget(source: T, target: T): List<E> {
        return edges.filterForSourceAndTarget(source, target);
    }

    override fun getEdgesWithLabel(label: EdgeLabel): List<E> {
        return edges.filterForLabel(label);
    }

    override fun getInitialNodes() : List<T> {
        return this.nodes.filter { it.isInitialState };
    }

    override fun getFinalNodes(): List<T> {
        return this.nodes.filter { it.isFinalState };
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Graph<*,*>) return false

        @Suppress("UNCHECKED_CAST")
        other as Graph<T, E>

        return hasEqualNodes(other) && hasEqualEdges(other) && (alphabet == other.alphabet)
    }

    fun hasEqualNodes(other: Graph<T,E>) : Boolean {
        if (nodes.count() != other.nodes.count()) return false
        nodes.forEach {
            val node = other.getNode(it.identifier);
            if (node !== null) {
                if (it != node) return false;
            } else return false;
        }
        return true;
    }

    fun hasEqualEdges(other: Graph<T,E>) : Boolean {
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
    abstract override fun addEdge(edge: E) : Boolean;
}