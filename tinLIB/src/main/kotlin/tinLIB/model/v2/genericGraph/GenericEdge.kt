package tinLIB.model.v2.genericGraph

import tinLIB.model.v2.graph.AbstractEdge
import tinLIB.model.v2.graph.Edge
import tinLIB.model.v2.graph.EdgeLabelProperty
import tinLIB.model.v2.graph.Node
import tinLIB.model.v2.query.QueryEdge
import tinLIB.model.v2.query.QueryEdgeLabel
import tinLIB.model.v2.transducer.TransducerEdge

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
