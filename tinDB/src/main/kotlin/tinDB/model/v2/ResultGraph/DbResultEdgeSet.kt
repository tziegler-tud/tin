package tinDB.model.v2.ResultGraph

import tinLIB.model.v2.ResultGraph.ResultEdgeSet

class DbResultEdgeSet: ResultEdgeSet<DbResultEdge>() {
    companion object {
        public fun fromList(list: List<DbResultEdge>) : DbResultEdgeSet {
            val set = DbResultEdgeSet();
            set.addAll(list);
            return set;
        }
    }
}