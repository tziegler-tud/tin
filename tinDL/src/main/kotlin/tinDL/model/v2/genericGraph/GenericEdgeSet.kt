package tinDL.model.v2.genericGraph

import tinDL.model.v2.graph.EdgeLabel
import tinDL.model.v2.graph.EdgeSet
import tinDL.model.v2.graph.Node
import tinDL.model.v2.query.QueryEdge

class GenericEdgeSet : EdgeSet<GenericEdge>() {
    companion object {
        public fun fromList(list: List<GenericEdge>) : GenericEdgeSet {
            val set = GenericEdgeSet();
            set.addAll(list);
            return set;
        }
    }

    override fun filterForSource(source: Node): List<GenericEdge> {
        return filter{it.source === source};
    }
    override fun filterForTarget(target: Node): List<GenericEdge> {
        return filter{it.target === target};
    }
    override fun filterForSourceAndTarget(source: Node, target: Node): List<GenericEdge> {
        return filter{it.source === source && it.target === target};
    }
    override fun filterForLabel(label: EdgeLabel): List<GenericEdge> {
        return filter{it.label === label};
    }
    override fun containsEdge(edge: GenericEdge): Boolean {
        for (genericEdge in this) {
            if(genericEdge == edge) {
                return true
            };
            else {
                continue
            }
        }
        return false;
    }

}