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

        return super.equals(other);
//        return this.checkForNodesEquality(other) &&
//                label == other.label
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
        return super.hashCode();
    }
}
