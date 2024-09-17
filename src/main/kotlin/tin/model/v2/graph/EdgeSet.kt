package tin.model.v2.graph

abstract class EdgeSet<Edge> : HashSet<Edge>() {

    abstract fun filterForSource(source: Node): List<Edge>;
    abstract fun filterForTarget(target: Node): List<Edge>;
    abstract fun filterForSourceAndTarget(source:Node, target: Node): List<Edge>;
    abstract fun filterForLabel(label: EdgeLabel): List<Edge>;
    abstract fun containsEdge(edge: Edge): Boolean;
}