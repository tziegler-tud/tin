package tinDL.model.v2.genericGraph

import tinDL.model.v2.graph.AbstractEdge
import tinDL.model.v2.graph.Edge
import tinDL.model.v2.graph.EdgeLabelProperty
import tinDL.model.v2.graph.Node
import tinDL.model.v2.query.QueryEdge
import tinDL.model.v2.query.QueryEdgeLabel
import tinDL.model.v2.transducer.TransducerEdge

class GenericEdge (
    override val source: Node,
    override val target: Node,
    override val label: GenericEdgeLabel,
) : AbstractEdge(source, target, label) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is GenericEdge) return false

        return checkForNodesEquality(other) &&
                label == other.label
    }

    override fun asGenericEdge(): GenericEdge {
        return this;
    }

    override fun hashCode(): Int {
        var result = source.identifier.hashCode()
        result = 31 * result + target.identifier.hashCode()
        result = 31 * result + label.hashCode()
        return result
    }
}
