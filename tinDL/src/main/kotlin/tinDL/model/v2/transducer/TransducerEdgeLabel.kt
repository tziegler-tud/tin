package tinDL.model.v2.transducer

import tinDL.model.v2.graph.EdgeLabel
import tinDL.model.v2.graph.EdgeLabelProperty
import tinDL.model.v2.query.QueryEdgeLabel

class TransducerEdgeLabel(
    val incoming: EdgeLabelProperty,
    val outgoing: EdgeLabelProperty,
    val cost: Int,
    ) : EdgeLabel
{
    constructor(incoming: String, outgoing: String, cost: Int) : this(EdgeLabelProperty.fromString(incoming), EdgeLabelProperty.fromString(outgoing), cost)

    override fun toString(): String {
        return "${incoming}|${outgoing}|${cost}";
    }

    override fun hashCode(): Int {
        var result = incoming.hashCode()
        result = 31 * result + outgoing.hashCode()
        result = 31 * result + cost.hashCode()
        return result;
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TransducerEdgeLabel) return false
        return incoming == other.incoming && outgoing == other.outgoing && cost == other.cost;
    }
}