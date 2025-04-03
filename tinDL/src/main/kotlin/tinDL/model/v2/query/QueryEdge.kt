package tinDL.model.v2.query

import tinDL.model.v2.graph.AbstractEdge
import tinDL.model.v2.graph.Edge
import tinDL.model.v2.graph.EdgeLabelProperty
import tinDL.model.v2.graph.Node
import tinDL.model.v2.query.QueryEdge
import tinDL.model.v2.transducer.TransducerEdge

class QueryEdge (
    override val source: Node,
    override val target: Node,
    override val label: QueryEdgeLabel
) : AbstractEdge(source, target, label) {

    constructor(source: Node, target: Node, stringLabel: String) : this(source, target, QueryEdgeLabel(EdgeLabelProperty.fromString(stringLabel)))



    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is QueryEdge) return false

        return checkForNodesEquality(other) &&
                label == other.label
    }

    private fun checkForNodesEquality(other: QueryEdge): Boolean {
        return source == other.source &&
                target == other.target
    }

    override fun asTransducerEdge(): TransducerEdge? {
        return null;
    }

    override fun asQueryEdge(): QueryEdge {
        return this;
    }


    override fun hashCode(): Int {
        var result = source.identifier.hashCode()
        result = 31 * result + target.identifier.hashCode()
        result = 31 * result + label.hashCode()
        return result
    }
}
