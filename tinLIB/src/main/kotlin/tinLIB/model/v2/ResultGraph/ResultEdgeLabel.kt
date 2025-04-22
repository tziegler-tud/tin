package tinLIB.model.v2.ResultGraph

import tinLIB.model.v2.graph.EdgeLabel

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