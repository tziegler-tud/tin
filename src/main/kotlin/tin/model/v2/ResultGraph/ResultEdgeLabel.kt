package tin.model.v2.ResultGraph

import tin.model.v2.graph.EdgeLabel
import tin.model.v2.graph.EdgeLabelProperty
import tin.model.v2.transducer.TransducerEdgeLabel

class ResultEdgeLabel(val cost: Int) : EdgeLabel {

    override fun toString(): String {
        return "$cost";
    }

    override fun hashCode(): Int {
        return cost.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ResultEdgeLabel) return false
        return cost == other.cost;
    }
}