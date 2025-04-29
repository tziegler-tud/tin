package tinDB.model.v2.DatabaseGraph

import tinLIB.model.v2.graph.AbstractEdge
import tinLIB.model.v2.graph.Edge
import tinLIB.model.v2.graph.EdgeLabelProperty
import tinLIB.model.v2.graph.Node
import tinLIB.model.v2.query.QueryEdgeLabel
import tinLIB.model.v2.transducer.TransducerEdge


class DatabaseEdge (
    override val source: Node,
    override val target: Node,
    override val label: DatabaseEdgeLabel
) : AbstractEdge(source, target, label) {

    constructor(source: Node, target: Node, stringLabel: String) : this(source, target, DatabaseEdgeLabel(EdgeLabelProperty.fromString(stringLabel)))

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DatabaseEdge) return false

        return checkForNodesEquality(other) &&
                label == other.label
    }

    private fun checkForNodesEquality(other: DatabaseEdge): Boolean {
        return source == other.source &&
                target == other.target
    }

    override fun asTransducerEdge(): TransducerEdge? {
        return null;
    }

    override fun toString(): String {
        return String.format("(%s) -[%s]-> (%s)", source.identifier, label, target.identifier)
    }


    override fun hashCode(): Int {
        var result = source.identifier.hashCode()
        result = 31 * result + target.identifier.hashCode()
        result = 31 * result + label.hashCode()
        return result
    }
}

