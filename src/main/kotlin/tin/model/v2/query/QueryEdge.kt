package tin.model.v2.query

import tin.model.v2.graph.Edge
import tin.model.v2.graph.Node

class QueryEdge (
    override var source: Node,
    override var target: Node,
    label: String
) : Edge(
        source, target, label
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is QueryEdge) return false

        return super.equals(other);
//        return this.checkForNodesEquality(other) &&
//                label == other.label
    }


    override fun hashCode(): Int {
        return super.hashCode();
    }
}
