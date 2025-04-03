package tinDL.model.v2.graph

import tinDL.model.v1.alphabet.Alphabet

abstract class AbstractGraph : tinDL.model.v2.graph.Graph {

    companion object {
        fun isValidGraph(graphLikeObject: Any?): Boolean {
            //must be a Graph
            if (graphLikeObject !is tinDL.model.v2.graph.Graph){
                return false;
            }
            //at least one initial node and one final node
            return graphLikeObject.hasInitialNode() && graphLikeObject.hasFinalNode();
        }
    }

    abstract override val nodes: tinDL.model.v2.graph.NodeSet;
    abstract override val edges: tinDL.model.v2.graph.EdgeSet<out tinDL.model.v2.graph.Edge>
    abstract override var alphabet: Alphabet;

    override fun addNode(node: _root_ide_package_.tinDL.model.v2.graph.Node) : Boolean {
        return nodes.add(node);
    }

    override fun addNodes(vararg n: _root_ide_package_.tinDL.model.v2.graph.Node) : Boolean {
        val list = listOf(*n);
        var allInserted = true
        list.forEach { allInserted = allInserted && addNode(it) }
        return allInserted;
    }

    override fun getNode(identifier: String) : _root_ide_package_.tinDL.model.v2.graph.Node? {
        return nodes.get(identifier)
    }

    override fun containsNode(identifier: String) : Boolean {
        return nodes.find { it.identifier == identifier } != null
    }

    override fun containsNode(node: _root_ide_package_.tinDL.model.v2.graph.Node) : Boolean {
        return nodes.contains(node)
    }

    override fun containsEdge(edge: _root_ide_package_.tinDL.model.v2.graph.Edge) : Boolean {
        return edges.contains(edge)
    }

    override fun getEdgesWithSource(source: _root_ide_package_.tinDL.model.v2.graph.Node): List<_root_ide_package_.tinDL.model.v2.graph.Edge> {
        return edges.filterForSource(source);
    }

    override fun getEdgesWithTarget(target: _root_ide_package_.tinDL.model.v2.graph.Node): List<_root_ide_package_.tinDL.model.v2.graph.Edge> {
        return edges.filterForTarget(target);
    }

    override fun getEdgesWithSourceAndTarget(source: _root_ide_package_.tinDL.model.v2.graph.Node, target: _root_ide_package_.tinDL.model.v2.graph.Node): List<_root_ide_package_.tinDL.model.v2.graph.Edge> {
        return edges.filterForSourceAndTarget(source, target);
    }

    override fun getEdgesWithLabel(label: _root_ide_package_.tinDL.model.v2.graph.EdgeLabel): List<_root_ide_package_.tinDL.model.v2.graph.Edge> {
        return edges.filterForLabel(label);
    }

    override fun getInitialNodes() : List<_root_ide_package_.tinDL.model.v2.graph.Node> {
        return this.nodes.filter { it.isInitialState };
    }

    override fun getFinalNodes(): List<_root_ide_package_.tinDL.model.v2.graph.Node> {
        return this.nodes.filter { it.isFinalState };
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is _root_ide_package_.tinDL.model.v2.graph.Graph) return false

        return hasEqualNodes(other) && hasEqualEdges(other) && (alphabet == other.alphabet)

    }

    fun hasEqualNodes(other: _root_ide_package_.tinDL.model.v2.graph.Graph) : Boolean {
        if (nodes.count() != other.nodes.count()) return false
        nodes.forEach {
            val node = other.getNode(it.identifier);
            if (node !== null) {
                if (it != node) return false;
            } else return false;
        }
        return true;
    }

    fun hasEqualEdges(other: _root_ide_package_.tinDL.model.v2.graph.Graph) : Boolean {
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
    abstract override fun addEdge(edge: _root_ide_package_.tinDL.model.v2.graph.Edge) : Boolean;
}