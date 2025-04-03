package tinDL.model.v2.query

import tinDL.model.v2.graph.EdgeLabel
import tinDL.model.v2.graph.EdgeLabelProperty
import tinDL.model.v2.transducer.TransducerEdgeLabel

class QueryEdgeLabel(val label: EdgeLabelProperty) : EdgeLabel {

    constructor(label: String) : this(EdgeLabelProperty.fromString(label))

    override fun toString(): String {
        return "$label";
    }

    override fun hashCode(): Int {
        return label.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is QueryEdgeLabel) return false
        return label == other.label;
    }
}