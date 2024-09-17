package tin.model.v2.query

import tin.model.v2.graph.Edge
import tin.model.v2.graph.EdgeLabel
import tin.model.v2.graph.EdgeLabelProperty
import tin.model.v2.graph.Node
import tin.model.v2.transducer.TransducerEdge

class QueryEdge (
    override val source: Node,
    override val target: Node,
    override val label: QueryEdgeLabel
) : Edge {

    constructor(source: Node, target: Node, stringLabel: String) : this(source, target, QueryEdgeLabel(EdgeLabelProperty.fromString(stringLabel)))

    override fun toString(): String {
        return "(${source.identifier}) - [${label}] - (${target.identifier})";
    }

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

    override fun print() {
        println(this)
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
