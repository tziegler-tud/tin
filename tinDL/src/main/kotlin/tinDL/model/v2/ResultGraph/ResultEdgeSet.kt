package tinDL.model.v2.ResultGraph

import tinDL.model.v2.graph.EdgeLabel
import tinDL.model.v2.graph.EdgeSet
import tinDL.model.v2.graph.Node
import tinDL.model.v2.query.QueryEdge

class ResultEdgeSet : EdgeSet<ResultEdge>() {
    companion object {
        public fun fromList(list: List<ResultEdge>) : ResultEdgeSet {
            val set = ResultEdgeSet();
            set.addAll(list);
            return set;
        }
    }

    override fun filterForSource(source: Node): List<ResultEdge> {
        return filter{it.source == source};
    }
    override fun filterForTarget(target: Node): List<ResultEdge> {
        return filter{it.target == target};
    }
    override fun filterForSourceAndTarget(source: Node, target: Node): List<ResultEdge> {
        return filter{it.source == source && it.target === target};
    }
    override fun filterForLabel(label: EdgeLabel): List<ResultEdge> {
        return filter{it.label == label};
    }
    override fun containsEdge(edge: ResultEdge): Boolean {
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