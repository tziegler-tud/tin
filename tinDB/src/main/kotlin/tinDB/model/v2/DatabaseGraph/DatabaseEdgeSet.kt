package tinDB.model.v2.DatabaseGraph

import tinLIB.model.v2.graph.EdgeLabel
import tinLIB.model.v2.graph.EdgeSet
import tinLIB.model.v2.graph.Node

class DatabaseEdgeSet : EdgeSet<DatabaseEdge>() {
    companion object {
        public fun fromList(list: List<DatabaseEdge>) : DatabaseEdgeSet {
            val set = DatabaseEdgeSet();
            set.addAll(list);
            return set;
        }
    }

    override fun filterForSource(source: Node): List<DatabaseEdge> {
        return filter{it.source == source};
    }
    override fun filterForTarget(target: Node): List<DatabaseEdge> {
        return filter{it.target == target};
    }
    override fun filterForSourceAndTarget(source: Node, target: Node): List<DatabaseEdge> {
        return filter{it.source == source && it.target === target};
    }
    override fun filterForLabel(label: EdgeLabel): List<DatabaseEdge> {
        return filter{it.label == label};
    }
    override fun containsEdge(edge: DatabaseEdge): Boolean {
        for (databaseEdge in this) {
            if(databaseEdge == edge) {
                return true
            };
            else {
                continue
            }
        }
        return false;
    }

}