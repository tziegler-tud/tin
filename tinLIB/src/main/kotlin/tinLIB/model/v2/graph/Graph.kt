package tinLIB.model.v2.graph

import tinLIB.model.v2.alphabet.Alphabet

interface Graph<T: Node, E: Edge> {

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

    val nodes: NodeSet<T>;
    val edges: EdgeSet<out E>
    var alphabet: Alphabet;

    fun addNode(node: T) : Boolean;

    fun addNodes(vararg n: T): Boolean;

    fun getNode(identifier: String) : T?

    fun containsNode(identifier: String) : Boolean;

    fun containsNode(node: T) : Boolean;

    fun addEdge(edge: E) : Boolean;

    fun containsEdge(edge: E) : Boolean;

    fun getEdgesWithSource(source: T): List<E>;

    fun getEdgesWithTarget(target: T): List<E>;

    fun getEdgesWithSourceAndTarget(source: T, target: T): List<E>;

    fun getEdgesWithLabel(label: EdgeLabel): List<E>;

    fun getInitialNodes() : List<T>

    fun getFinalNodes(): List<T>

    override fun equals(other: Any?): Boolean;

    override fun hashCode(): Int;

    fun isEmpty(): Boolean;

    fun isValidGraph(): Boolean;

    fun hasInitialNode(): Boolean;

    fun hasFinalNode(): Boolean;

    fun printGraph();
}