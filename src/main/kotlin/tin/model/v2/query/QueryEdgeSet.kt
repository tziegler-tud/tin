package tin.model.v2.query

import tin.model.v2.graph.EdgeLabel
import tin.model.v2.graph.EdgeSet
import tin.model.v2.graph.Node
import tin.model.v2.query.QueryEdge

class QueryEdgeSet : EdgeSet<QueryEdge>() {
    companion object {
        public fun fromList(list: List<QueryEdge>) : QueryEdgeSet {
            val set = QueryEdgeSet();
            set.addAll(list);
            return set;
        }
    }

    override fun filterForSource(source: Node): List<QueryEdge> {
        return filter{it.source == source};
    }
    override fun filterForTarget(target: Node): List<QueryEdge> {
        return filter{it.target == target};
    }
    override fun filterForSourceAndTarget(source: Node, target: Node): List<QueryEdge> {
        return filter{it.source == source && it.target === target};
    }
    override fun filterForLabel(label: EdgeLabel): List<QueryEdge> {
        return filter{it.label == label};
    }
    override fun containsEdge(edge: QueryEdge): Boolean {
        for (queryEdge in this) {
            if(queryEdge == edge) {
                return true
            };
            else {
                continue
            }
        }
        return false;
    }

}