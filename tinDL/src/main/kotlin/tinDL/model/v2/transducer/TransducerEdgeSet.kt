package tinDL.model.v2.transducer

import tinDL.model.v2.graph.EdgeLabel
import tinDL.model.v2.graph.EdgeSet
import tinDL.model.v2.graph.Node

class TransducerEdgeSet : EdgeSet<TransducerEdge>() {
    companion object {
        public fun fromList(list: List<TransducerEdge>) : TransducerEdgeSet {
            val set = TransducerEdgeSet();
            set.addAll(list);
            return set;
        }
    }

    override fun filterForSource(source: Node): List<TransducerEdge> {
        return filter{it.source == source};
    }
    override fun filterForTarget(target: Node): List<TransducerEdge> {
        return filter{it.target == target};
    }
    override fun filterForSourceAndTarget(source: Node, target: Node): List<TransducerEdge> {
        return filter{it.source == source && it.target === target};
    }
    override fun filterForLabel(label: EdgeLabel): List<TransducerEdge> {
        return filter{it.label == label};
    }

    override fun containsEdge(edge: TransducerEdge): Boolean {
        for (transducerEdge in this) {
            if(transducerEdge == edge) return true;
        }
        return false;
    }
}