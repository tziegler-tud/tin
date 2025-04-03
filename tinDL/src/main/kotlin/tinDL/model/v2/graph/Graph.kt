package tinDL.model.v2.graph

import tinDL.model.v1.alphabet.Alphabet

interface Graph {

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

    val nodes: NodeSet;
    val edges: EdgeSet<out Edge>
    var alphabet: Alphabet;

    fun addNode(node: Node) : Boolean;

    fun addNodes(vararg n: Node): Boolean;

    fun getNode(identifier: String) : Node?

    fun containsNode(identifier: String) : Boolean;

    fun containsNode(node: Node) : Boolean;

    fun addEdge(edge: Edge) : Boolean;

    fun containsEdge(edge: Edge) : Boolean;

    fun getEdgesWithSource(source: Node): List<Edge>;

    fun getEdgesWithTarget(target: Node): List<Edge>;

    fun getEdgesWithSourceAndTarget(source: Node, target: Node): List<Edge>;

    fun getEdgesWithLabel(label: EdgeLabel): List<Edge>;

    fun getInitialNodes() : List<Node>

    fun getFinalNodes(): List<Node>

    override fun equals(other: Any?): Boolean;

    override fun hashCode(): Int;

    fun isEmpty(): Boolean;

    fun isValidGraph(): Boolean;

    fun hasInitialNode(): Boolean;

    fun hasFinalNode(): Boolean;

    fun printGraph();
}