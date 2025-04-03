package tinDL.model.v2.genericGraph

import tinDL.model.v2.graph.EdgeLabel
import tinDL.model.v2.graph.EdgeLabelProperty
import tinDL.model.v2.transducer.TransducerEdgeLabel

class GenericEdgeLabel(val label: Int) : EdgeLabel {

    override fun toString(): String {
        return "$label";
    }

    override fun hashCode(): Int {
        return label.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is GenericEdgeLabel) return false
        return label == other.label;
    }
}