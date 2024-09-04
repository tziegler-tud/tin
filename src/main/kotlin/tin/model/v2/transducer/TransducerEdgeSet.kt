package tin.model.v2.transducer

import tin.model.v2.graph.EdgeSet
import tin.model.v2.graph.Node
import tin.model.v2.query.QueryEdge

class TransducerEdgeSet : EdgeSet<TransducerEdge>() {
    companion object {
        public fun fromList(list: List<TransducerEdge>) : TransducerEdgeSet {
            val set = TransducerEdgeSet();
            set.addAll(list);
            return set;
        }
    }

    override fun filterForSource(source: Node): List<TransducerEdge> {
        return filter{it.source === source};
    }
    override fun filterForTarget(target: Node): List<TransducerEdge> {
        return filter{it.target === target};
    }
    override fun filterForSourceAndTarget(source: Node, target: Node): List<TransducerEdge> {
        return filter{it.source === source && it.target === target};
    }
    override fun filterForLabel(label: String): List<TransducerEdge> {
        return filter{it.label === label};
    }
}