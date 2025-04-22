package tinLIB.model.v2.ResultGraph

import tinLIB.model.v2.graph.EdgeLabel
import tinLIB.model.v2.graph.EdgeSet
import tinLIB.model.v2.graph.Node

open class ResultEdgeSet<T: ResultEdge> : EdgeSet<T>() {

    override fun filterForSource(source: Node): List<T> {
        return filter{it.source == source};
    }
    override fun filterForTarget(target: Node): List<T> {
        return filter{it.target == target};
    }
    override fun filterForSourceAndTarget(source: Node, target: Node): List<T> {
        return filter{it.source == source && it.target === target};
    }
    override fun filterForLabel(label: EdgeLabel): List<T> {
        return filter{it.label == label};
    }
    override fun containsEdge(edge: T): Boolean {
        for (resultEdge in this) {
            if(resultEdge == edge) {
                return true
            };
            else {
                continue
            }
        }
        return false;
    }

}