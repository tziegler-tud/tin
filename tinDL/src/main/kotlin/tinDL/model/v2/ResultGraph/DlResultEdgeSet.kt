package tinDL.model.v2.ResultGraph

import tinLIB.model.v2.ResultGraph.ResultEdgeSet

class DlResultEdgeSet: ResultEdgeSet<DlResultEdge>() {
    companion object {
        public fun fromList(list: List<DlResultEdge>) : DlResultEdgeSet {
            val set = DlResultEdgeSet();
            set.addAll(list);
            return set;
        }
    }
}