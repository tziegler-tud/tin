package tinDB.model.v1.query

import tinDB.model.v1.graph.Edge

class QueryEdge (
    override var source: QueryNode,
    override var target: QueryNode,
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
